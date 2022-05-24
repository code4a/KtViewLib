package com.jiangyt.library.logger.helper

import com.jiangyt.library.logger.LogLevel
import com.jiangyt.library.logger.Utils
import com.jiangyt.library.logger.adapter.LogAdapter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * @Title: NTCloud
 * @Package com.jiangyt.library.logger.helper
 * @Description: 日志帮助实现类
 * @author apple
 * @date 2022/5/24 5:37 下午
 * @version V1.0
 */
internal class LogHelper : Helper {

    companion object {
        /**
         * It is used for json pretty print
         */
        private const val JSON_INDENT = 2
    }

    /**
     * Provides one-time used tag for the log message
     */
    private val localTag = ThreadLocal<String>()

    private val logAdapters: ArrayList<LogAdapter> = ArrayList()


    override fun addAdapter(adapter: LogAdapter) {
        logAdapters.add(adapter)
    }

    override fun t(tag: String?): Helper {
        if (tag != null) {
            localTag.set(tag)
        }
        return this
    }

    override fun d(message: String, vararg args: Any?) {
        log(LogLevel.DEBUG, null, message, args)
    }

    override fun d(obj: Any?) {
        log(LogLevel.DEBUG, null, Utils.toString(obj))
    }

    override fun e(message: String, vararg args: Any?) {
        e(null, message, *args)
    }

    override fun e(throwable: Throwable?, message: String, vararg args: Any?) {
        log(LogLevel.ERROR, throwable, message, args)
    }

    override fun w(message: String, vararg args: Any?) {
        log(LogLevel.WARN, null, message, args)
    }

    override fun i(message: String, vararg args: Any?) {
        log(LogLevel.INFO, null, message, args)
    }

    override fun v(message: String, vararg args: Any?) {
        log(LogLevel.VERBOSE, null, message, args)
    }

    override fun wtf(message: String, vararg args: Any?) {
        log(LogLevel.ASSERT, null, message, args)
    }

    override fun json(json: String?) {
        if (Utils.isEmpty(json)) {
            d("Empty/Null json content")
            return
        }
        try {
            val jsonF = json!!.trim { it <= ' ' }
            if (jsonF.startsWith("{")) {
                val jsonObject = JSONObject(jsonF)
                val message = jsonObject.toString(JSON_INDENT)
                d(message)
                return
            }
            if (jsonF.startsWith("[")) {
                val jsonArray = JSONArray(jsonF)
                val message = jsonArray.toString(JSON_INDENT)
                d(message)
                return
            }
            e("Invalid Json")
        } catch (e: JSONException) {
            e("Invalid Json")
        }
    }

    override fun xml(xml: String?) {
        if (Utils.isEmpty(xml)) {
            d("Empty/Null xml content")
            return
        }
        try {
            val xmlInput: Source = StreamSource(StringReader(xml))
            val xmlOutput = StreamResult(StringWriter())
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transformer.transform(xmlInput, xmlOutput)
            d(xmlOutput.writer.toString().replaceFirst(">".toRegex(), ">\n"))
        } catch (e: TransformerException) {
            e("Invalid xml")
        }
    }

    @Synchronized
    override fun log(logLevel: Int, tag: String?, msg: String?, throwable: Throwable?) {
        var message = msg
        if (throwable != null && message != null) {
            message += " : " + Utils.getStackTraceString(throwable)
        }
        if (throwable != null && message == null) {
            message = Utils.getStackTraceString(throwable)
        }
        if (Utils.isEmpty(message)) {
            message = "Empty/NULL log message"
        }

        for (adapter in logAdapters) {
            if (adapter.isLoggable(logLevel, tag!!)) {
                adapter.log(logLevel, tag, message!!)
            }
        }
    }

    override fun clearLogAdapters() {
        logAdapters.clear()
    }


    /**
     * This method is synchronized in order to avoid messy of logs' order.
     */
    @Synchronized
    private fun log(
        priority: Int,
        throwable: Throwable?,
        msg: String,
        vararg args: Any?
    ) {
        val tag = getTag()
        val message = createMessage(msg, *args)
        log(priority, tag, message, throwable)
    }

    /**
     * @return the appropriate tag based on local or global
     */
    private fun getTag(): String? {
        val tag = localTag.get()
        if (tag != null) {
            localTag.remove()
            return tag
        }
        return null
    }

    private fun createMessage(message: String, vararg args: Any?): String {
        return if (args.isEmpty()) message else String.format(message, *args)
    }

}