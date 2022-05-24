package com.jiangyt.library.logger.writer

import java.io.File

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.writer
 * @Description: 将日志写入到文件中
 * @author apple
 * @date 2022/5/24 11:30 上午
 * @version V1.0
 */
abstract class Writer {

    /**
     * 打开一个特定的日志文件以备将来写入，如果它还不存在，只需创建它。
     * @param file 特定的日志文件，可能不存在
     * @return 如果日志文件成功打开，则为 true，否则为 false
     */
    abstract fun open(file: File): Boolean

    /**
     * 在之前的 {@link #open(File)} 中是否成功打开了一个日志文件。
     * @return 如果打开日志文件，则为 true，否则为 false
     */
    abstract fun isOpened(): Boolean

    /**
     * 获取打开的日志文件。
     *
     * @return 打开的日志文件，如果日志文件没有打开，则返回 null
     */
    abstract fun getOpenedFile(): File?

    /**
     * 获取打开的日志文件名称。
     *
     * @return 打开的日志文件名称，如果日志文件没有打开，则返回 null
     */
    abstract fun getOpenedFileName(): String?

    /**
     * 将日志附加到打开的日志文件的末尾，通常需要额外的行分隔符。
     *
     * @param log 添加的日志
     */
    abstract fun appendLog(log: String)

    /**
     * 确保打开的日志文件已关闭，通常在切换日志文件之前调用。
     *
     * @return 如果日志文件成功关闭，则为 true，否则为 false
     */
    abstract fun close(): Boolean
}