package taboolib.common5.util

fun String.startsWithAny(vararg prefix: String): Boolean {
    return prefix.any { startsWith(it) }
}

fun String.endsWithAny(vararg suffix: String): Boolean {
    return suffix.any { endsWith(it) }
}

fun String.substringAfterAny(vararg morePrefix: String): String {
    return substringAfter(morePrefix.firstOrNull { startsWith(it) } ?: return this)
}

fun String.substringBeforeAny(vararg moreSuffix: String): String {
    return substringBefore(moreSuffix.firstOrNull { endsWith(it) } ?: return this)
}

fun String.replace(vararg pairs: Pair<String, Any>): String {
    var text = this
    pairs.forEach { pair ->
        text = text.replace(pair.first, pair.second.toString())
    }
    return text
}

fun List<String>.replace(vararg pairs: Pair<String, Any>): List<String> {
    return map { it.replace(*pairs) }
}