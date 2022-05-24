package com.jiangyt.library.logger.flattener

import com.jiangyt.library.logger.LogLevel

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.flattener
 * @Description: 只需将时间戳、日志级别、标签和消息连接在一起。
 * @author apple
 * @date 2022/5/24 3:42 下午
 * @version V1.0
 */
class DefaultFlattener : Flattener {
    override fun flatten(
        timeMillis: Long,
        logLevel: Int,
        tag: String,
        message: String
    ): CharSequence {
        return (timeMillis.toString()
                + '|' + LogLevel.getShortLevelName(logLevel)
                + '|'.toString() + tag
                + '|'.toString() + message)
    }
}