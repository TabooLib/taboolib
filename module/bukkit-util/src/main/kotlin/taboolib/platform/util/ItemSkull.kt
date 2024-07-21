package taboolib.platform.util

import org.bukkit.inventory.meta.SkullMeta
import taboolib.library.xseries.profiles.builder.XSkull
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern

private val pattern = Pattern.compile("(http://.*?)\"")

fun SkullMeta.getSkullValue(): String? {
    return XSkull.of(this).profileValue
}

fun SkullMeta.getSkinUrl(): String? {
    val json = Base64.getDecoder().decode(getSkullValue()).toString(StandardCharsets.UTF_8)
    val matcher = pattern.matcher(json)
    return if (matcher.find()) matcher.group(1) else null
}