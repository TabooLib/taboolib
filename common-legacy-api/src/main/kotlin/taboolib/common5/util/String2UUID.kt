package taboolib.common5.util

import java.util.*

fun String.parseUUID(): UUID? {
    return runCatching { UUID.fromString(this) }.getOrNull()
}