package com.jiangyt.library.logger.helper

import com.jiangyt.library.logger.adapter.LogAdapter

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.helper
 * @Description: 日志打印帮助类
 * @author apple
 * @date 2022/5/24 5:34 下午
 * @version V1.0
 */
interface Helper {
    fun addAdapter(adapter: LogAdapter)

    fun t(tag: String?): Helper

    fun d(message: String, vararg args: Any?)

    fun d(obj: Any?)

    fun e(message: String, vararg args: Any?)

    fun e(throwable: Throwable?, message: String, vararg args: Any?)

    fun w(message: String, vararg args: Any?)

    fun i(message: String, vararg args: Any?)

    fun v(message: String, vararg args: Any?)

    fun wtf(message: String, vararg args: Any?)

    /**
     * Formats the given json content and print it
     */
    fun json(json: String?)

    /**
     * Formats the given xml content and print it
     */
    fun xml(xml: String?)

    fun log(logLevel: Int, tag: String?, msg: String?, throwable: Throwable?)

    fun clearLogAdapters()
}