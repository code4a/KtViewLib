package com.jiangyt.sample.ktview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jiangyt.library.logger.Logger
import com.jiangyt.library.logger.adapter.FileLogAdapter
import com.jiangyt.library.logger.adapter.LogcatAdapter
import com.jiangyt.library.logger.printer.PrettyLogPrinter
import com.jiangyt.library.logger.printer.Printer
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Logger.addLogAdapter(LogcatAdapter())
        Logger.d("message")

        Logger.clearLogAdapters()


        var formatStrategy: Printer = PrettyLogPrinter.newBuilder()
            .showThreadInfo(false) // (Optional) Whether to show thread info or not. Default true
            .methodCount(0) // (Optional) How many method line to show. Default 2
            .methodOffset(3) // (Optional) Skips some method invokes in stack trace. Default 5
            //        .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
            .tag("My custom tag") // (Optional) Custom tag for each log. Default PRETTY_LOGGER
            .build()

        Logger.addLogAdapter(LogcatAdapter(formatStrategy))

        Logger.addLogAdapter(object : LogcatAdapter() {
            override fun isLoggable(logLevel: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })

        Logger.addLogAdapter(FileLogAdapter())


        Logger.w("no thread info and only 1 method")

        Logger.clearLogAdapters()
        formatStrategy = PrettyLogPrinter.newBuilder()
            .showThreadInfo(false)
            .methodCount(0)
            .build()

        Logger.addLogAdapter(LogcatAdapter(formatStrategy))
        Logger.i("no thread info and method info")

        Logger.t("tag").e("Custom tag for only one use")

        Logger.json("{ \"key\": 3, \"value\": something}")

        Logger.d(Arrays.asList("foo", "bar"))

        val map: MutableMap<String, String> = HashMap()
        map["key"] = "value"
        map["key1"] = "value2"

        Logger.d(map)

        Logger.clearLogAdapters()
        formatStrategy = PrettyLogPrinter.newBuilder()
            .showThreadInfo(false)
            .methodCount(0)
            .tag("MyTag")
            .build()
        Logger.addLogAdapter(LogcatAdapter(formatStrategy))

        Logger.w("my log message with my tag")
    }
}