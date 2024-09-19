package taboolib.platform.util

import org.bukkit.inventory.meta.SkullMeta
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern

private val pattern = Pattern.compile("(http://.*?)\"")

/**
 * 获取头颅的 Base64 编码值。
 * @return 头颅的 Base64 编码值，如果为空则返回 null。
 */
fun SkullMeta.getSkullValue(): String? {
    return BukkitSkull.getSkullValue(this).takeIf { it.isNotEmpty() }
}

/**
 * 获取头颅皮肤的 URL。
 * @return 头颅皮肤的 URL，如果无法获取则返回 null。
 */
fun SkullMeta.getSkinUrl(): String? {
    val json = Base64.getDecoder().decode(getSkullValue()).toString(StandardCharsets.UTF_8)
    val matcher = pattern.matcher(json)
    return if (matcher.find()) matcher.group(1) else null
}