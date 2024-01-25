package taboolib.common5.util

import java.util.*

fun String.parseUUID(): UUID? {
    return kotlin.runCatching { UUID.fromString(this) }.getOrNull()
}