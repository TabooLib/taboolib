package taboolib.common5.util

import java.util.*

fun String.parseUUID(): UUID? {
    return try {
        UUID.fromString(this)
    } catch (_: IllegalArgumentException) {
        null
    }
}