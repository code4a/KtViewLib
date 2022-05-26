package com.jiangyt.library.logger

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Process
import com.jiangyt.library.logger.naming.DateFileNameGenerator
import com.jiangyt.library.logger.printer.FilePrinter
import com.jiangyt.library.logger.printer.Printer
import com.jiangyt.library.logger.writer.SimpleWriter
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 崩溃配置，日志默认位置 externalCacheDir
 */
internal object LogCrash {

    //General constants
    private const val HANDLER_PACKAGE_NAME = "com.jiangyt.library.logger"
    private const val DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os"
    private const val NAME_PREFIX = "crash"
    private const val TIME_TO_CONSIDER_FOREGROUND_MS = 500
    private const val MAX_ACTIVITIES_IN_LOG = 50

    //Shared preferences
    private const val SHARED_PREFERENCES_FILE = "custom_activity_on_crash"
    private const val SHARED_PREFERENCES_FIELD_TIMESTAMP = "last_crash_timestamp"

    private val activityLog: Deque<String> = ArrayDeque(MAX_ACTIVITIES_IN_LOG)
    private var lastActivityCreatedTimestamp = 0L
    private var isInBackground = true

    private lateinit var application: Application
    private lateinit var filePrinter: Printer

    fun install(context: Context, logRootPath: String) {
        try {
            val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
            if (oldHandler != null && oldHandler.javaClass.name.startsWith(HANDLER_PACKAGE_NAME)) {
                Logger.e(
                    "LogCrash 已经安装了，什么都不做！"
                )
            } else {
                if (oldHandler != null && !oldHandler.javaClass.name.startsWith(
                        DEFAULT_HANDLER_PACKAGE_NAME
                    )
                ) {
                    Logger.e("IMPORTANT WARNING! 你已经有一个 UncaughtExceptionHandler，如果使用自定义 UncaughtExceptionHandler，则必须在 LogCrash 之后对其进行初始化！无论如何都要安装，但不会调用您的原始处理程序。")
                }
                application = context.applicationContext as Application

                //我们定义了一个默认的异常处理程序来执行我们想要的操作，以便可以从 Crashlytics/ACRA 调用它
                Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                    Logger.e("应用已经崩溃，正在执行LogCrash的UncaughtExceptionHandler", throwable)
                    if (hasCrashedInTheLastSeconds(application)) {
                        Logger.e(
                            "应用程序最近已经崩溃，没有启动自定义错误活动，因为我们可以进入重启循环。您确定您的应用程序不会直接在初始化时崩溃吗？",
                            throwable
                        )
                        if (oldHandler != null) {
                            oldHandler.uncaughtException(thread, throwable)
                            return@setDefaultUncaughtExceptionHandler
                        }
                    } else {
                        filePrinter = FilePrinter.newBuilder()
                            // 指定保存日志文件的路径
                            .setFolderPath(File(logRootPath, NAME_PREFIX).path)
                            .setFileNameGenerator(DateFileNameGenerator(NAME_PREFIX)) // Default: DateFileNameGenerator("log")
                            // .cleanStrategy(new FileLastModifiedCleanStrategy(MAX_TIME))     // Default: NeverCleanStrategy()
                            //.setFlattener(DefaultFlattener()) // Default: DefaultFlattener
                            .setWriter(object : SimpleWriter() {
                                // Default: SimpleWriter
                                override fun onNewFileCreated(file: File?) {
                                    val header = """
                             >>>>>>>>>>>>>>>> File Header >>>>>>>>>>>>>>>>
                             Device Manufacturer: ${Build.MANUFACTURER}
                             Device Model       : ${Build.MODEL}
                             Android Version    : ${Build.VERSION.RELEASE}
                             Android SDK        : ${Build.VERSION.SDK_INT}
                             App VersionName    : ${Utils.getVersionName(context)}
                             App VersionCode    : ${Utils.getVersionCode(context)}
                             <<<<<<<<<<<<<<<< File Header <<<<<<<<<<<<<<<<
                           
                             """.trimIndent()
                                    appendLog(header)
                                }
                            })
                            .setUseWorker(false)
                            .build()

                        setLastCrashTimestamp(application, Date().time)
                        if (!isInBackground || lastActivityCreatedTimestamp >= Date().time - TIME_TO_CONSIDER_FOREGROUND_MS) {
                            Logger.d("收集崩溃相关信息，准备写入到本应用的崩溃信息收集日志中")
                            val sw = StringWriter()
                            val pw = PrintWriter(sw)
                            throwable.printStackTrace(pw)
                            val stackTraceString = sw.toString()
                            Logger.d(stackTraceString)
                            val activityLogSb = StringBuilder()
                            while (!activityLog.isEmpty()) {
                                activityLogSb.append(activityLog.poll())
                            }
                            val als = activityLogSb.toString()
                            Logger.d(als)
                            // 将错误信息写入日志文件
                            errorMsgWriteToFile(stackTraceString, als)
                        } else {
                            Logger.d("不符合本应用的收集规则，使用原有的分发器处理异常信息")
                        }
                        if (oldHandler != null) {
                            oldHandler.uncaughtException(thread, throwable)
                            return@setDefaultUncaughtExceptionHandler
                        }
                    }

//                    killCurrentProcess()
                }

                application.registerActivityLifecycleCallbacks(
                    object : ActivityLifecycleCallbacks {
                        var currentlyStartedActivities = 0
                        val dateFormat: DateFormat =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

                        override fun onActivityCreated(
                            activity: Activity,
                            savedInstanceState: Bundle?
                        ) {
                            lastActivityCreatedTimestamp = Date().time
                            activityLog.add("${dateFormat.format(Date())}: ${activity.javaClass.simpleName} created")
                        }

                        override fun onActivityStarted(activity: Activity) {
                            currentlyStartedActivities++
                            isInBackground = currentlyStartedActivities == 0
                            //Do nothing
                        }

                        override fun onActivityResumed(activity: Activity) {
                            activityLog.add("${dateFormat.format(Date())}: ${activity.javaClass.simpleName} resumed")
                        }

                        override fun onActivityPaused(activity: Activity) {
                            activityLog.add("${dateFormat.format(Date())}: ${activity.javaClass.simpleName} paused")
                        }

                        override fun onActivityStopped(activity: Activity) {
                            //Do nothing
                            currentlyStartedActivities--
                            isInBackground =
                                currentlyStartedActivities == 0
                        }

                        override fun onActivitySaveInstanceState(
                            activity: Activity,
                            outState: Bundle
                        ) {
                            //Do nothing
                        }

                        override fun onActivityDestroyed(activity: Activity) {
                            activityLog.add("${dateFormat.format(Date())}: ${activity.javaClass.simpleName} destroyed")
                        }
                    })

                Logger.i("LogCrash has been installed.")
            }
        } catch (t: Throwable) {
            Logger.e(t, "install crash")
        }
    }

    private fun errorMsgWriteToFile(
        stackTrace: String,
        activityLog: String
    ) {
        // 错误的栈信息写入文件
        filePrinter.println(LogLevel.ERROR, "StackTrace", stackTrace)
        // 将activity启动流程写入文件中
        filePrinter.println(LogLevel.ERROR, "ActivityLife", activityLog)
        Logger.d("应用的崩溃信息写入本应用的崩溃信息收集日志中")
    }


    /**
     * 杀死当前进程。
     * 它在重新启动或终止应用程序后使用。
     */
    private fun killCurrentProcess() {
        Process.killProcess(Process.myPid())
        System.exit(10)
    }

    /**
     * 存储上次崩溃时间戳
     *
     * @param timestamp 当前时间戳。
     */
    @SuppressLint("ApplySharedPref") //This must be done immediately since we are killing the app
    private fun setLastCrashTimestamp(context: Context, timestamp: Long) {
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
            .edit().putLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, timestamp).commit()
    }

    /**
     * 获取最后的崩溃时间戳
     *
     * @return 最后一次崩溃时间戳，如果未设置，则为 -1。
     */
    private fun getLastCrashTimestamp(context: Context): Long {
        return context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
            .getLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, -1)
    }

    /**
     * 这告诉应用程序是否在最后几秒钟内崩溃。这用于避免重启循环。
     * @return 如果应用程序在最后几秒钟内崩溃，则为 true，否则为 false。
     */
    private fun hasCrashedInTheLastSeconds(context: Context): Boolean {
        val lastTimestamp: Long = getLastCrashTimestamp(context)
        val currentTimestamp = Date().time
        return lastTimestamp <= currentTimestamp && currentTimestamp - lastTimestamp < 3 * 1000
    }
}