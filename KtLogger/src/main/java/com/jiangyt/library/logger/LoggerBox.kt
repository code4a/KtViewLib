package com.jiangyt.library.logger

import android.content.Context
import android.os.Build
import com.jiangyt.library.logger.adapter.FileLogAdapter
import com.jiangyt.library.logger.adapter.LogcatAdapter
import com.jiangyt.library.logger.printer.FilePrinter
import com.jiangyt.library.logger.printer.PrettyLogPrinter
import com.jiangyt.library.logger.printer.Printer
import com.jiangyt.library.logger.writer.SimpleWriter
import java.io.File

object LoggerBox {

    /**
     * 使用默认配置快速初始化，不使用当前方法时可以自行组合配置
     */
    fun quickConfig(
        context: Context,
        showLog: Boolean = true,
        logToFile: Boolean = showLog,
        crashCollect: Boolean = logToFile
    ) {
        if (showLog) {
            val logPrinter: Printer = PrettyLogPrinter.newBuilder()
                .showThreadInfo(false) // （可选）是否显示线程信息。默认为真
                .methodCount(0) // （可选）显示多少个方法行。默认 2
                .build()
            Logger.addLogAdapter(LogcatAdapter(logPrinter))
        }

        if (logToFile) {
            val filePrinter: Printer = FilePrinter.newBuilder()
                // 指定保存日志文件的路径
                .setFolderPath(File(context.filesDir.absolutePath, "log").path)
                // .cleanStrategy(new FileLastModifiedCleanStrategy(MAX_TIME))
                .setWriter(object : SimpleWriter() {
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
                .build()
            Logger.addLogAdapter(FileLogAdapter(filePrinter))
        }

        if (crashCollect) {
            LogCrash.install(context, context.filesDir.absolutePath)
        }
    }
}