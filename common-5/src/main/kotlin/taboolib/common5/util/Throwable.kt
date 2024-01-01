@file:Isolated
@file:Suppress("NOTHING_TO_INLINE")

package taboolib.common5.util

import taboolib.common.Isolated
import java.io.PrintWriter
import java.io.StringWriter

inline fun Throwable.getStackTraceString(): String {
    val sw = StringWriter()
    printStackTrace(PrintWriter(sw))
    return sw.toString()
}

inline fun Throwable.getStackTraceStringList(): List<String> {
    val sw = StringWriter()
    printStackTrace(PrintWriter(sw))
    return sw.toString().lines()
}