package com.jiangyt.library.logger.printer

import android.os.Environment
import android.util.Log
import com.jiangyt.library.logger.clean.CleanStrategy
import com.jiangyt.library.logger.clean.FileLastModifiedCleanStrategy
import com.jiangyt.library.logger.flattener.DefaultFlattener
import com.jiangyt.library.logger.flattener.Flattener
import com.jiangyt.library.logger.naming.DateFileNameGenerator
import com.jiangyt.library.logger.naming.FileNameGenerator
import com.jiangyt.library.logger.writer.SimpleWriter
import com.jiangyt.library.logger.writer.Writer
import java.io.File
import java.util.concurrent.LinkedBlockingQueue

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger
 * @Description: 日志打印到文件
 * @author apple
 * @date 2022/5/24 9:34 上午
 * @version V1.0
 */
class FilePrinter constructor(builder: Builder) : Printer {

    companion object {
        private const val USE_WORKER = true

        /**
         * 默认3天
         */
        const val DEF_FILE_MAX_LIFE: Long = 3 * 24 * 60 * 60 * 1000

        fun newBuilder(): Builder {
            return Builder()
        }
    }

    private val folderPath: String
    private val fileNameGenerator: FileNameGenerator
    private val cleanStrategy: CleanStrategy
    private val flattener: Flattener
    private val writer: Writer

    @Volatile
    private lateinit var worker: Worker

    init {
        folderPath = builder.folderPath!!
        fileNameGenerator = builder.fileNameGenerator!!
        cleanStrategy = builder.cleanStrategy!!
        flattener = builder.flattener!!
        writer = builder.writer!!

        if (USE_WORKER) {
            worker = Worker()
        }
    }

    class Builder internal constructor() {

        internal var folderPath: String? = null
        internal var fileNameGenerator: FileNameGenerator? = null
        internal var cleanStrategy: CleanStrategy? = null
        internal var flattener: Flattener? = null
        internal var writer: Writer? = null

        fun setFolderPath(folderPath: String): Builder {
            this.folderPath = folderPath
            return this
        }

        fun setWriter(writer: Writer): Builder {
            this.writer = writer
            return this
        }

        fun setFileNameGenerator(fileNameGenerator: FileNameGenerator): Builder {
            this.fileNameGenerator = fileNameGenerator
            return this
        }

        fun setCleanStrategy(cleanStrategy: CleanStrategy): Builder {
            this.cleanStrategy = cleanStrategy
            return this
        }

        fun setFlattener(flattener: Flattener): Builder {
            this.flattener = flattener
            return this
        }

        fun build(): FilePrinter {
            fillEmptyFields()
            return FilePrinter(this)
        }

        private fun fillEmptyFields() {
            if (folderPath == null) {
                val diskPath = Environment.getDownloadCacheDirectory().absolutePath
                folderPath = diskPath + File.separatorChar + "logger"
            }
            if (fileNameGenerator == null) {
                fileNameGenerator = DateFileNameGenerator()
            }
            if (cleanStrategy == null) {
                cleanStrategy = FileLastModifiedCleanStrategy(DEF_FILE_MAX_LIFE)
            }
            if (flattener == null) {
                flattener = DefaultFlattener()
            }
            if (writer == null) {
                writer = SimpleWriter()
            }
        }
    }

    override fun println(logLevel: Int, tag: String, msg: String) {
        val timeMillis = System.currentTimeMillis()
        if (USE_WORKER) {
            if (!worker.isStarted()) {
                worker.start()
            }
            worker.enqueue(LogItem(timeMillis, logLevel, tag, msg))
        } else {
            doPrintln(timeMillis, logLevel, tag, msg)
        }
    }

    fun doPrintln(timeMillis: Long, logLevel: Int, tag: String, msg: String) {
        var lastFileName = writer.getOpenedFileName()
        val isWriterClosed = !writer.isOpened()
        if (lastFileName == null || isWriterClosed || fileNameGenerator.isFileNameChangeable()) {
            val newFileName =
                fileNameGenerator.generateFileName(logLevel, System.currentTimeMillis())
            if (newFileName.trim { it <= ' ' }.isEmpty()) {
                Log.e("NT-Log", "File name should not be empty, ignore log: $msg")
                return
            }
            if (newFileName != lastFileName || isWriterClosed) {
                writer.close()
                cleanLogFilesIfNecessary()
                if (!writer.open(File(folderPath, newFileName))) {
                    return
                }
                lastFileName = newFileName
            }
        }

//        val lastFile = writer.getOpenedFile()
//        if (backupStrategy.shouldBackup(lastFile)) {
//            // Backup the log file, and create a new log file.
//            writer.close()
//            BackupUtil.backup(lastFile, backupStrategy)
//            if (!writer.open(File(folderPath, lastFileName))) {
//                return
//            }
//        }
        val flattenedLog: String = flattener.flatten(timeMillis, logLevel, tag, msg).toString()
        writer.appendLog(flattenedLog)
    }

    private fun cleanLogFilesIfNecessary() {
        val logDir = File(folderPath)
        val files = logDir.listFiles() ?: return
        for (file in files) {
            if (cleanStrategy.shouldClean(file)) {
                file.delete()
            }
        }
    }

    private inner class Worker : Runnable {

        private val logs = LinkedBlockingQueue<LogItem>()

        @Volatile
        private var started = false

        /**
         * 将日志添加到队列中
         */
        fun enqueue(log: LogItem) {
            try {
                logs.put(log)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        /**
         * worker是否开始
         */
        fun isStarted(): Boolean {
            synchronized(this) { return started }
        }

        /**
         * 开始
         */
        fun start() {
            synchronized(this) {
                if (started) {
                    return
                }
                Thread(this).start()
                started = true
            }
        }

        override fun run() {
            var log: LogItem
            try {
                while (logs.take().also { log = it } != null) {
                    doPrintln(log.timeMillis, log.level, log.tag, log.msg)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                synchronized(this) { started = false }
            }
        }

    }

    private data class LogItem(
        val timeMillis: Long,
        val level: Int,
        val tag: String,
        val msg: String
    )
}
