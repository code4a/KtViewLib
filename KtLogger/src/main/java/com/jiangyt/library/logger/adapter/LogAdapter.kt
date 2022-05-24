package com.jiangyt.library.logger.adapter

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.adapter
 * @Description: 日志适配器
 * @author apple
 * @date 2022/5/24 4:23 下午
 * @version V1.0
 */
interface LogAdapter {
    /**
     * Used to determine whether log should be printed out or not.
     *
     * @param logLevel is the log level e.g. DEBUG, WARNING
     * @param tag is the given tag for the log message
     *
     * @return is used to determine if log should printed.
     * If it is true, it will be printed, otherwise it'll be ignored.
     */
    fun isLoggable(logLevel: Int, tag: String?): Boolean

    /**
     * Each log will use this pipeline
     *
     * @param logLevel is the log level e.g. DEBUG, WARNING
     * @param tag is the given tag for the log message.
     * @param msg is the given message for the log message.
     */
    fun log(logLevel: Int, tag: String, msg: String)
}