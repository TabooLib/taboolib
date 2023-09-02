package taboolib.expansion.fx

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.chat.colored
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.isAir
import taboolib.platform.util.modifyLore
import java.util.*


fun ItemStack?.ifAir(): ItemStack? {
    if (this == null) {
        return null
    }
    if (this.isAir) {
        return null
    }
    if (this.type == Material.AIR) {
        return null
    }
    return this
}

fun ItemStack.getString(key: String, def: String = "null"): String {
    if (this.isAir) {
        return def
    }
    return this.getItemTag().getDeepOrElse(key, ItemTagData(def)).asString()
}

fun ItemStack.getInt(key: String, def: Int = -1): Int {
    if (this.isAir) {
        return def
    }
    return this.getItemTag().getDeepOrElse(key, ItemTagData(def)).asInt()
}

fun ItemStack.getDouble(key: String, def: Double = -1.0): Double {
    if (this.isAir) {
        return def
    }
    return this.getItemTag().getDeepOrElse(key, ItemTagData(def)).asDouble()
}

fun ItemStack.getStringList(key: String): List<String> {
    if (this.isAir) {
        return listOf()
    }
    return this.getItemTag().getDeep(key)?.asList()?.map { it.asString() } ?: listOf()
}

fun ItemStack.getDoubleList(key: String): List<Double> {
    if (this.isAir) {
        return listOf()
    }
    return this.getItemTag().getDeep(key)?.asList()?.map { it.asDouble() } ?: listOf()
}

fun ItemStack.getIntList(key: String): List<Int> {
    if (this.isAir) {
        return listOf()
    }
    return this.getItemTag().getDeep(key)?.asList()?.map { it.asInt() } ?: listOf()
}

fun ItemStack.set(key: String, value: Any?) {
    val tag = getItemTag()
    if (value == null) {
        tag.removeDeep(key)
    } else {
        if (value is UUID) {
            tag.putDeep(key, value.toString())
        }
        tag.putDeep(key, value)
    }
    tag.saveTo(this)
}

fun ItemStack.replacePapi(player: Player): ItemStack {
    modifyLore {
        val replacePlaceholder = this.replacePlaceholder(player)
        clear()
        addAll(replacePlaceholder.colored())
    }
    return this
}
