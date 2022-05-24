package com.jiangyt.library.logger.adapter

import com.jiangyt.library.logger.printer.PrettyLogPrinter
import com.jiangyt.library.logger.printer.Printer

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.adapter
 * @Description: 控制台打印适配器
 * @author apple
 * @date 2022/5/24 5:17 下午
 * @version V1.0
 */
open class LogcatAdapter(private val printer: Printer = PrettyLogPrinter.newBuilder().build()) :
    LogAdapter {

    override fun isLoggable(logLevel: Int, tag: String): Boolean {
        return true
    }

    override fun log(logLevel: Int, tag: String, msg: String) {
        printer.println(logLevel, tag, msg)
    }
}