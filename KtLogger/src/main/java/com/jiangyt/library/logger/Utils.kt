package com.jiangyt.library.logger

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException


internal object Utils {
    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    fun isEmpty(str: CharSequence?): Boolean {
        return str == null || str.isEmpty()
    }

    /**
     * Returns true if a and b are equal, including if they are both null.
     *
     * *Note: In platform versions 1.1 and earlier, this method only worked well if
     * both the arguments were instances of String.*
     *
     * @param a first CharSequence to check
     * @param b second CharSequence to check
     * @return true if a and b are equal
     *
     *
     * NOTE: Logic slightly change due to strict policy on CI -
     * "Inner assignments should be avoided"
     */
    fun equals(a: CharSequence?, b: CharSequence?): Boolean {
        if (a === b) return true
        if (a != null && b != null) {
            val length = a.length
            if (length == b.length) {
                return if (a is String && b is String) {
                    a == b
                } else {
                    for (i in 0 until length) {
                        if (a[i] != b[i]) return false
                    }
                    true
                }
            }
        }
        return false
    }

    /**
     * Copied from "android.util.Log.getStackTraceString()" in order to avoid usage of Android stack
     * in unit tests.
     *
     * @return Stack trace in form of String
     */
    fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) {
            return ""
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        var t = tr
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    fun toString(obj: Any?): String {
        if (obj == null) {
            return "null"
        }
        if (!obj.javaClass.isArray) {
            return obj.toString()
        }
        if (obj is BooleanArray) {
            return obj.contentToString()
        }
        if (obj is ByteArray) {
            return obj.contentToString()
        }
        if (obj is CharArray) {
            return obj.contentToString()
        }
        if (obj is ShortArray) {
            return obj.contentToString()
        }
        if (obj is IntArray) {
            return obj.contentToString()
        }
        if (obj is LongArray) {
            return obj.contentToString()
        }
        if (obj is FloatArray) {
            return obj.contentToString()
        }
        if (obj is DoubleArray) {
            return obj.contentToString()
        }
        return if (obj is Array<*>) {
            obj.contentDeepToString()
        } else "Couldn't find a correct type for the object"
    }

    /**
     * 获取版本名称
     */
    fun getVersionName(context: Context): String {
        return try {
            val pi: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pi.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "UnKnown"
        }
    }

    /**
     * 获取版本号
     */
    fun getVersionCode(context: Context): Long {
        return try {
            val pi: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pi.longVersionCode
            } else pi.versionCode.toLong()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            0
        }
    }
}


