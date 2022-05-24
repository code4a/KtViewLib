package com.jiangyt.library.logger.naming

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.naming
 * @Description: 文件名称生成
 * @author apple
 * @date 2022/5/24 2:29 下午
 * @version V1.0
 */
interface FileNameGenerator {
    /**
     * 生成的文件名是否会改变。
     *
     * @return 如果文件名是可更改的，则为 true
     */
    fun isFileNameChangeable(): Boolean

    /**
     * 为指定的日志级别和时间戳生成文件名。
     *
     * @param logLevel  日志级别
     * @param timestamp 记录发生时的时间戳
     * @return 生成的文件名
     */
    fun generateFileName(logLevel: Int, timestamp: Long): String
}