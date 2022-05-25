package com.jiangyt.library.logger.naming

import java.text.SimpleDateFormat
import java.util.*

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.naming
 * @Description: 根据时间戳生成文件名，不同的日期会导致不同的文件名。
 * @author apple
 * @date 2022/5/24 2:31 下午
 * @version V1.0
 */
class DateFileNameGenerator(private val prefix: String = "log") : FileNameGenerator {

    private val mLocalDateFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("yyyy-MM-dd", Locale.US)
        }
    }

    override fun isFileNameChangeable(): Boolean {
        return true
    }

    /**
     * 生成代表特定日期的文件名。
     */
    override fun generateFileName(logLevel: Int, timestamp: Long): String {
        val sdf = mLocalDateFormat.get()
        sdf.timeZone = TimeZone.getDefault()
        return String.format("%s_%s.log", prefix, sdf.format(Date(timestamp)))
    }
}