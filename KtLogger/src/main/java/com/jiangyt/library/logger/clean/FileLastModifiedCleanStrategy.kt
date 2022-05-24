package com.jiangyt.library.logger.clean

import java.io.File

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.clean
 * @Description: 文件的最长保留时间
 * @author apple
 * @date 2022/5/24 2:44 下午
 * @version V1.0
 */
class FileLastModifiedCleanStrategy(private val maxTimeMillis: Long) : CleanStrategy {

    override fun shouldClean(file: File): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        val lastModified = file.lastModified()
        return currentTimeMillis - lastModified > maxTimeMillis
    }
}