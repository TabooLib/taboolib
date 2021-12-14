package taboolib.module.configuration.util

import java.util.regex.Pattern

val pattern = Pattern.compile("([A-Z])").toRegex()

fun String.smallHumpToHyphen(): String {
    return this.replace(pattern, "-$1").lowercase()
}
