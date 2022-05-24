package com.jiangyt.library.logger

import com.jiangyt.library.logger.adapter.LogAdapter
import com.jiangyt.library.logger.helper.Helper
import com.jiangyt.library.logger.helper.LogHelper


/**
 * <pre>
 *  ┌────────────────────────────────────────────
 *  │ LOGGER
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ Standard logging mechanism
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ But more pretty, simple and powerful
 *  └────────────────────────────────────────────
 * </pre>
 *
 * <h3>How to use it</h3>
 * Initialize it first
 * <pre><code>
 *   Logger.addLogAdapter(new AndroidLogAdapter());
 * </code></pre>
 *
 * And use the appropriate static Logger methods.
 *
 * <pre><code>
 *   Logger.d("debug");
 *   Logger.e("error");
 *   Logger.w("warning");
 *   Logger.v("verbose");
 *   Logger.i("information");
 *   Logger.wtf("What a Terrible Failure");
 * </code></pre>
 *
 * <h3>String format arguments are supported</h3>
 * <pre><code>
 *   Logger.d("hello %s", "world");
 * </code></pre>
 *
 * <h3>Collections are support ed(only available for debug logs)</h3>
 * <pre><code>
 *   Logger.d(MAP);
 *   Logger.d(SET);
 *   Logger.d(LIST);
 *   Logger.d(ARRAY);
 * </code></pre>
 *
 * <h3>Json and Xml support (output will be in debug level)</h3>
 * <pre><code>
 *   Logger.json(JSON_CONTENT);
 *   Logger.xml(XML_CONTENT);
 * </code></pre>
 *
 * <h3>Customize Logger</h3>
 * Based on your needs, you can change the following settings:
 * <ul>
 *   <li>Different {@link LogAdapter}</li>
 *   <li>Different {@link Printer}</li>
 * </ul>
 *
 * @see LogAdapter
 * @see Printer
 */
object Logger {

    private var helper: Helper = LogHelper()

    fun setHelper(helper: Helper) {
        Logger.helper = helper
    }

    fun addLogAdapter(adapter: LogAdapter) {
        helper.addAdapter(adapter)
    }

    fun clearLogAdapters() {
        helper.clearLogAdapters()
    }

    /**
     * Given tag will be used as tag only once for this method call regardless of the tag that's been
     * set during initialization. After this invocation, the general tag that's been set will
     * be used for the subsequent log calls
     */
    fun t(tag: String?): Helper {
        return helper.t(tag)
    }

    /**
     * General log function that accepts all configurations as parameter
     */
    fun log(priority: Int, tag: String?, message: String?, throwable: Throwable?) {
        helper.log(priority, tag, message, throwable)
    }

    fun d(message: String, vararg args: Any?) {
        helper.d(message, args)
    }

    fun d(`object`: Any?) {
        helper.d(`object`)
    }

    fun e(message: String, vararg args: Any?) {
        helper.e(null, message, args)
    }

    fun e(throwable: Throwable?, message: String, vararg args: Any?) {
        helper.e(throwable, message, args)
    }

    fun i(message: String, vararg args: Any?) {
        helper.i(message, args)
    }

    fun v(message: String, vararg args: Any?) {
        helper.v(message, args)
    }

    fun w(message: String, vararg args: Any?) {
        helper.w(message, args)
    }

    /**
     * Tip: Use this for exceptional situations to log
     * ie: Unexpected errors etc
     */
    fun wtf(message: String, vararg args: Any?) {
        helper.wtf(message, args)
    }

    /**
     * Formats the given json content and print it
     */
    fun json(json: String?) {
        helper.json(json)
    }

    /**
     * Formats the given xml content and print it
     */
    fun xml(xml: String?) {
        helper.xml(xml)
    }

}