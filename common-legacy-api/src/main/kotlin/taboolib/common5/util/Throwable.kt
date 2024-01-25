package taboolib.common5.util

import java.io.PrintWriter
import java.io.StringWriter

fun Throwable.getStackTraceString(): String {
    val sw = StringWriter()
    printStackTrace(PrintWriter(sw))
    return sw.toString()
}

fun Throwable.getStackTraceStringList(): List<String> {
    val sw = StringWriter()
    printStackTrace(PrintWriter(sw))
    return sw.toString().lines()
}