package com.jiangyt.library.logger

internal object LogLevel {

    /**
     * Log level for NTLogger.v.
     */
    const val VERBOSE = 2

    /**
     * Log level for NTLogger.d.
     */
    const val DEBUG = 3

    /**
     * Log level for NTLogger.i.
     */
    const val INFO = 4

    /**
     * Log level for NTLogger.w.
     */
    const val WARN = 5

    /**
     * Log level for NTLogger.e.
     */
    const val ERROR = 6

    const val ASSERT = 7

    /**
     * Log level for NTLogger#init, printing all logs.
     */
    const val ALL = Int.MIN_VALUE

    /**
     * Log level for NTLogger#init, printing no log.
     */
    const val NONE = Int.MAX_VALUE

    /**
     * Get a name representing the specified log level.
     *
     *
     * The returned name may be<br></br>
     * Level less than [VERBOSE]: "VERBOSE-N", N means levels below
     * [VERBOSE]<br></br>
     * [VERBOSE]: "VERBOSE"<br></br>
     * [DEBUG]: "DEBUG"<br></br>
     * [INFO]: "INFO"<br></br>
     * [WARN]: "WARN"<br></br>
     * [ERROR]: "ERROR"<br></br>
     * [ASSERT]: "ASSERT"<br></br>
     * Level greater than [ERROR]: "ERROR+N", N means levels above
     * [ERROR]
     *
     * @param logLevel the log level to get name for
     * @return the name
     */
    fun getLevelName(logLevel: Int): String {
        return when (logLevel) {
            VERBOSE -> "VERBOSE"
            DEBUG -> "DEBUG"
            INFO -> "INFO"
            WARN -> "WARN"
            ERROR -> "ERROR"
            ASSERT -> "ASSERT"
            else -> if (logLevel < VERBOSE) {
                "VERBOSE-" + (VERBOSE - logLevel)
            } else {
                "ERROR+" + (logLevel - ERROR)
            }
        }
    }

    /**
     * Get a short name representing the specified log level.
     *
     *
     * The returned name may be<br></br>
     * Level less than [VERBOSE]: "V-N", N means levels below
     * [VERBOSE]<br></br>
     * [VERBOSE]: "V"<br></br>
     * [DEBUG]: "D"<br></br>
     * [INFO]: "I"<br></br>
     * [WARN]: "W"<br></br>
     * [ERROR]: "E"<br></br>
     * Level greater than [ERROR]: "E+N", N means levels above
     * [ERROR]
     *
     * @param logLevel the log level to get short name for
     * @return the short name
     */
    fun getShortLevelName(logLevel: Int): String {
        return when (logLevel) {
            VERBOSE -> "V"
            DEBUG -> "D"
            INFO -> "I"
            WARN -> "W"
            ERROR -> "E"
            ASSERT -> "A"
            else -> if (logLevel < VERBOSE) {
                "V-" + (VERBOSE - logLevel)
            } else {
                "E+" + (logLevel - ERROR)
            }
        }
    }
}