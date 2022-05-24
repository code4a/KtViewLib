package com.jiangyt.library.logger.writer

import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.writer
 * @Description: 默认的文件写入类
 * @author apple
 * @date 2022/5/24 11:43 上午
 * @version V1.0
 */
class SimpleWriter : Writer() {
    /**
     * 打开的日志文件的名称。
     */
    private var logFileName: String? = null

    /**
     * 打开的日志文件。
     */
    private var logFile: File? = null

    private var bufferedWriter: BufferedWriter? = null

    override fun open(file: File): Boolean {
        this.logFileName = file.name
        this.logFile = file
        var isNewFile = false

        // Create log file if not exists.
        if (!logFile!!.exists()) {
            isNewFile = try {
                val parent = logFile!!.parentFile
                if (!parent.exists()) {
                    parent.mkdirs()
                }
                logFile!!.createNewFile()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                close()
                return false
            }
        }

        // Create buffered writer.
        try {
            bufferedWriter = BufferedWriter(FileWriter(logFile, true))
            if (isNewFile) {
                onNewFileCreated(logFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            close()
            return false
        }
        return true
    }

    override fun isOpened(): Boolean {
        return logFile?.exists() ?: false
    }

    override fun getOpenedFile(): File? {
        return logFile
    }

    override fun getOpenedFileName(): String? {
        return logFileName
    }

    /**
     * 在新创建日志文件后调用。
     * 可以对新文件做一些初始化工作，比如调用[.appendLog]添加文件头。
     * 在工作线程中调用。
     *
     * @param file 新创建的日志文件
     */
    fun onNewFileCreated(file: File?) {}

    override fun appendLog(log: String) {
        bufferedWriter?.run {
            try {
                write(log)
                newLine()
                flush()
            } catch (e: Exception) {
                Log.w("NT-Log", "append log failed: " + e.message)
            }
        }
    }

    override fun close(): Boolean {
        bufferedWriter?.let {
            try {
                it.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        bufferedWriter = null
        logFileName = null
        logFile = null
        return true
    }
}