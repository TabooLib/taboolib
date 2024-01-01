@file:Isolated
@file:Suppress("NOTHING_TO_INLINE")

package taboolib.common5.util

import taboolib.common.Isolated
import java.util.*

inline fun String.parseUUID(): UUID? {
    return kotlin.runCatching { UUID.fromString(this) }.getOrNull()
}