package com.jiangyt.library.logger.printer

import android.text.TextUtils
import com.jiangyt.library.logger.Logger
import com.jiangyt.library.logger.helper.LogHelper

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger
 * @Description: 带格式的日志打印
 * @author apple
 * @date 2022/5/24 4:36 下午
 * @version V1.0
 */
class PrettyLogPrinter constructor(builder: Builder) : Printer {

    companion object {
        /**
         * Android's max limit for a log entry is ~4076 bytes,
         * so 4000 bytes is used as chunk size since default charset
         * is UTF-8
         */
        private const val CHUNK_SIZE = 4000

        /**
         * The minimum stack trace index, starts at this class after two native calls.
         */
        private const val MIN_STACK_OFFSET = 5

        /**
         * Drawing toolbox
         */
        private const val TOP_LEFT_CORNER = '┌'
        private const val BOTTOM_LEFT_CORNER = '└'
        private const val MIDDLE_CORNER = '├'
        private const val HORIZONTAL_LINE = '│'
        private const val DOUBLE_DIVIDER =
            "────────────────────────────────────────────────────────"
        private const val SINGLE_DIVIDER =
            "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
        private const val TOP_BORDER = TOP_LEFT_CORNER.toString() + DOUBLE_DIVIDER + DOUBLE_DIVIDER
        private const val BOTTOM_BORDER =
            BOTTOM_LEFT_CORNER.toString() + DOUBLE_DIVIDER + DOUBLE_DIVIDER
        private const val MIDDLE_BORDER = MIDDLE_CORNER.toString() + SINGLE_DIVIDER + SINGLE_DIVIDER

        fun newBuilder(): Builder {
            return Builder()
        }
    }

    private val methodCount: Int
    private val methodOffset: Int
    private val showThreadInfo: Boolean
    private val tag: String

    init {
        this.methodCount = builder.methodCount
        this.methodOffset = builder.methodOffset
        this.showThreadInfo = builder.showThreadInfo
        this.tag = builder.tag
    }


    override fun println(logLevel: Int, tag: String, msg: String) {
        val fTag = formatTag(tag)

        logTopBorder(logLevel, fTag)
        logHeaderContent(logLevel, fTag, methodCount)

        //get bytes of message with system's default charset (which is UTF-8 for Android)
        val bytes: ByteArray = msg.toByteArray()
        val length = bytes.size
        if (length <= CHUNK_SIZE) {
            if (methodCount > 0) {
                logDivider(logLevel, fTag)
            }
            logContent(logLevel, fTag, msg)
            logBottomBorder(logLevel, fTag)
            return
        }
        if (methodCount > 0) {
            logDivider(logLevel, fTag)
        }

        var i = 0
        while (i < length) {
            val count = (length - i).coerceAtMost(CHUNK_SIZE)
            //create a new String with system's default charset (which is UTF-8 for Android)
            logContent(logLevel, fTag, String(bytes, i, count))
            i += CHUNK_SIZE
        }

        logBottomBorder(logLevel, fTag)
    }

    private fun logTopBorder(logType: Int, tag: String) {
        logChunk(logType, tag, TOP_BORDER)
    }

    private fun logHeaderContent(logType: Int, tag: String, count: Int) {
        var methodCount = count
        val trace = Thread.currentThread().stackTrace
        if (showThreadInfo) {
            logChunk(
                logType,
                tag,
                HORIZONTAL_LINE.toString() + " Thread: " + Thread.currentThread().name
            )
            logDivider(logType, tag)
        }
        var level = ""
        val stackOffset = getStackOffset(trace) + methodOffset

        //corresponding method count with the current stack may exceeds the stack trace. Trims the count
        if (methodCount + stackOffset > trace.size) {
            methodCount = trace.size - stackOffset - 1
        }
        for (i in methodCount downTo 1) {
            val stackIndex = i + stackOffset
            if (stackIndex >= trace.size) {
                continue
            }
            val builder = StringBuilder()
            builder.append(HORIZONTAL_LINE)
                .append(' ')
                .append(level)
                .append(getSimpleClassName(trace[stackIndex].className))
                .append(".")
                .append(trace[stackIndex].methodName)
                .append(" ")
                .append(" (")
                .append(trace[stackIndex].fileName)
                .append(":")
                .append(trace[stackIndex].lineNumber)
                .append(")")
            level += "   "
            logChunk(logType, tag, builder.toString())
        }
    }

    private fun logBottomBorder(logType: Int, tag: String) {
        logChunk(logType, tag, BOTTOM_BORDER)
    }

    private fun logDivider(logType: Int, tag: String) {
        logChunk(logType, tag, MIDDLE_BORDER)
    }

    private fun logContent(logType: Int, tag: String, chunk: String) {
        val lines = chunk.split(System.getProperty("line.separator").toRegex()).toTypedArray()
        for (line in lines) {
            logChunk(
                logType,
                tag,
                "$HORIZONTAL_LINE $line"
            )
        }
    }

    private fun logChunk(priority: Int, tag: String, chunk: String) {
        android.util.Log.println(priority, tag, chunk)
    }

    private fun getSimpleClassName(name: String): String {
        val lastIndex = name.lastIndexOf(".")
        return name.substring(lastIndex + 1)
    }

    /**
     * Determines the starting index of the stack trace, after method calls made by this class.
     *
     * @param trace the stack trace
     * @return the stack offset
     */
    private fun getStackOffset(trace: Array<StackTraceElement>): Int {
        var i: Int = MIN_STACK_OFFSET
        while (i < trace.size) {
            val e = trace[i]
            val name = e.className
            if (name != LogHelper::class.java.name && name != Logger::class.java.name) {
                return --i
            }
            i++
        }
        return -1
    }

    private fun formatTag(tag: String): String {
        return if (!TextUtils.isEmpty(tag) && !TextUtils.equals(this.tag, tag)) {
            this.tag + "-" + tag
        } else this.tag
    }

    class Builder internal constructor() {
        internal var methodCount = 2
        internal var methodOffset = 0
        internal var showThreadInfo = true
        internal var tag = Logger.DEF_TAG

        fun methodCount(methodCount: Int): Builder {
            this.methodCount = methodCount
            return this
        }

        fun methodOffset(methodOffset: Int): Builder {
            this.methodOffset = methodOffset
            return this
        }

        fun showThreadInfo(showThreadInfo: Boolean): Builder {
            this.showThreadInfo = showThreadInfo
            return this
        }

        fun tag(tag: String): Builder {
            this.tag = tag
            return this
        }

        fun build(): PrettyLogPrinter {
            return PrettyLogPrinter(this)
        }
    }
}