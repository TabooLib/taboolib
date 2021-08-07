package taboolib.module.nms

import com.mojang.authlib.GameProfile
import org.bukkit.inventory.meta.SkullMeta
import taboolib.common.reflect.Reflex.Companion.getProperty
import java.util.*
import java.util.regex.Pattern

/**
 * @author xbaimiao
 * 头颅工具
 */
fun SkullMeta.getBase64(): String? {
    val gameProfile = this.getProperty<GameProfile>("profile") ?: return null
    val property = gameProfile.properties["textures"] ?: return null
    for (property1 in property) {
        return property1.value
    }
    return null
}

/**
 * @author xbaimiao
 * 解析头颅皮肤链接
 */
fun SkullMeta.getSkinUrl(): String? {
    val json = String(Base64.getDecoder().decode(this.getBase64()))
    val pattern = Pattern.compile("(http://.*?)\"")
    val matcher = pattern.matcher(json)
    if (matcher.find()) {
        return matcher.group(1)
    }
    return null
}