package com.jiangyt.library.logger.clean

import java.io.File

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.clean
 * @Description: 日志删除策略
 * @author apple
 * @date 2022/5/24 2:42 下午
 * @version V1.0
 */
interface CleanStrategy {
    /**
     * 是否应该清理指定的日志文件。
     *
     * @param file 日志文件
     * @return true，需要清理日志文件
     */
    fun shouldClean(file: File): Boolean
}