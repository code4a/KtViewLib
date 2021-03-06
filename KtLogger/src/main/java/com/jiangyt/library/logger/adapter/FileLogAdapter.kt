package com.jiangyt.library.logger.adapter

import android.Manifest.permission
import androidx.annotation.RequiresPermission
import com.jiangyt.library.logger.printer.FilePrinter
import com.jiangyt.library.logger.printer.Printer

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.adapter
 * @Description: 打印到日志的适配器
 * @author apple
 * @date 2022/5/24 5:21 下午
 * @version V1.0
 */
class FileLogAdapter(
    private val printer: Printer = FilePrinter.newBuilder().build()
) : LogAdapter {
    override fun isLoggable(logLevel: Int, tag: String): Boolean {
        return true
    }

    @RequiresPermission(anyOf = [permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE])
    override fun log(logLevel: Int, tag: String, msg: String) {
        printer.println(logLevel, tag, msg)
    }
}