package com.jiangyt.sample.ktview

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jiangyt.library.logger.Logger
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Logger.e(throwable, thread.name)
        }

        Logger.quickConfig(this, showLog = true, logToFile = true, crashCollect = true)

//        Logger.addLogAdapter(LogcatAdapter())
        Logger.d("message")

//        Logger.clearLogAdapters()


//        var formatStrategy: Printer = PrettyLogPrinter.newBuilder()
//            .showThreadInfo(false) // (Optional) Whether to show thread info or not. Default true
//            .methodCount(0) // (Optional) How many method line to show. Default 2
//            .methodOffset(3) // (Optional) Skips some method invokes in stack trace. Default 5
//            //        .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
//            .tag("My custom tag") // (Optional) Custom tag for each log. Default PRETTY_LOGGER
//            .build()
//
//        Logger.addLogAdapter(LogcatAdapter(formatStrategy))
//
//        Logger.addLogAdapter(object : LogcatAdapter() {
//            override fun isLoggable(logLevel: Int, tag: String): Boolean {
//                return BuildConfig.DEBUG
//            }
//        })


//        val filePrinter: Printer = FilePrinter.newBuilder()
////            .setFolderPath(
////                File(externalCacheDir!!.absolutePath, "log").path
////            ) // Specify the path to save log file
//            //.setFileNameGenerator(DateFileNameGenerator()) // Default: ChangelessFileNameGenerator("log")
//            // .backupStrategy(new MyBackupStrategy())             // Default: FileSizeBackupStrategy(1024 * 1024)
//            // .cleanStrategy(new FileLastModifiedCleanStrategy(MAX_TIME))     // Default: NeverCleanStrategy()
//            //.setFlattener(DefaultFlattener()) // Default: DefaultFlattener
//            .setWriter(object : SimpleWriter() {
//                // Default: SimpleWriter
//                override fun onNewFileCreated(file: File?) {
//                    val header = """
//
//             >>>>>>>>>>>>>>>> File Header >>>>>>>>>>>>>>>>
//             Device Manufacturer: ${Build.MANUFACTURER}
//             Device Model       : ${Build.MODEL}
//             Android Version    : ${Build.VERSION.RELEASE}
//             Android SDK        : ${Build.VERSION.SDK_INT}
//             App VersionName    : ${BuildConfig.VERSION_NAME}
//             App VersionCode    : ${BuildConfig.VERSION_CODE}
//             <<<<<<<<<<<<<<<< File Header <<<<<<<<<<<<<<<<
//
//             """.trimIndent()
//                    appendLog(header)
//                }
//            })
//            .build()
//        Logger.addLogAdapter(FileLogAdapter(filePrinter))

        Logger.w("no thread info and only 1 method")

//        Logger.clearLogAdapters()
//        formatStrategy = PrettyLogPrinter.newBuilder()
//            .showThreadInfo(false)
//            .methodCount(0)
//            .build()
//
//        Logger.addLogAdapter(LogcatAdapter(formatStrategy))
//
//        Logger.addLogAdapter(FileLogAdapter())
        Logger.i("no thread info and method info")

        Logger.t("tag").e("Custom tag for only one use")

        Logger.json("{ \"key\": 3, \"value\": something}")

        Logger.d(Arrays.asList("foo", "bar"))

        val map: MutableMap<String, String> = HashMap()
        map["key"] = "value"
        map["key1"] = "value2"

        Logger.d(map)

//        Logger.clearLogAdapters()
//        formatStrategy = PrettyLogPrinter.newBuilder()
//            .showThreadInfo(false)
//            .methodCount(0)
//            .tag("MyTag")
//            .build()
//        Logger.addLogAdapter(LogcatAdapter(formatStrategy))

        Logger.w("my log message with my tag")

        findViewById<View>(R.id.rating_star_view).postDelayed(
            {
                throw RuntimeException("测试崩溃日志收集")
            },
            5 * 1000
        )
    }
}