package com.jiangyt.library.logger.printer

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger
 * @Description: 日志打印
 * @author apple
 * @date 2022/5/24 9:32 上午
 * @version V1.0
 */
interface Printer {

    /**
     * 在新的一行打印日志
     *
     * @param logLevel log等级
     * @param tag      tag
     * @param msg      msg
     */
    fun println(logLevel: Int, tag: String, msg: String)
}