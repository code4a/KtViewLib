package com.jiangyt.library.logger.flattener

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.flattener
 * @Description:用于将日志元素（日志时间毫秒、级别、标签和消息）展平为单个 CharSequence。
 * @author apple
 * @date 2022/5/24 3:36 下午
 * @version V1.0
 */
interface Flattener {
    /**
     * 压平日志.
     *
     * @param timeMillis 日志的时间/毫秒
     * @param logLevel  日志登记
     * @param tag       日志时间
     * @param message   日志消息
     * @return 格式化的最终日志字符序列
     */
    fun flatten(timeMillis: Long, logLevel: Int, tag: String, message: String): CharSequence
}