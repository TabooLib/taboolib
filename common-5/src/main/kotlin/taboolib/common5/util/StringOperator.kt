@file:Isolated
@file:Suppress("NOTHING_TO_INLINE")

package taboolib.common5.util

import taboolib.common.Isolated

inline fun String.startsWithAny(vararg prefix: String): Boolean {
    return prefix.any { startsWith(it) }
}

inline fun String.endsWithAny(vararg suffix: String): Boolean {
    return suffix.any { endsWith(it) }
}

inline fun String.substringAfterAny(vararg morePrefix: String): String {
    return substringAfter(morePrefix.firstOrNull { startsWith(it) } ?: return this)
}

inline fun String.substringBeforeAny(vararg moreSuffix: String): String {
    return substringBefore(moreSuffix.firstOrNull { endsWith(it) } ?: return this)
}

inline fun String.replace(vararg pairs: Pair<String, Any>): String {
    var text = this
    pairs.forEach { pair ->
        text = text.replace(pair.first, pair.second.toString())
    }
    return text
}

inline fun List<String>.replace(vararg pairs: Pair<String, Any>): List<String> {
    return map { it.replace(*pairs) }
}