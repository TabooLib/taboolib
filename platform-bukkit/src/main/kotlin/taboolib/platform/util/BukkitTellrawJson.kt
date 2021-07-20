package taboolib.platform.util

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.common.reflect.Reflex.Companion.reflexInvoke
import taboolib.common.reflect.Reflex.Companion.staticInvoke
import taboolib.module.chat.TellrawJson

fun TellrawJson.hoverItem(itemStack: ItemStack): TellrawJson {
    val nmsItemStack = classCraftItemStack.staticInvoke<Any>("asNMSCopy", this)!!
    val nmsKey = try {
        itemStack.type.key.key
    } catch (ex: NoSuchMethodError) {
        val nmsItem = nmsItemStack.reflexInvoke<Any>("getItem")!!
        val name = nmsItem.reflex<String>("name")!!
        var key = ""
        name.forEach { c ->
            if (c.isUpperCase()) {
                key += "_" + c.lowercase()
            } else {
                key += c
            }
        }
        key
    }
    return hoverItem(nmsKey, nmsItemStack.reflexInvoke<Any>("getTag")?.toString() ?: "{}")
}

private val classCraftItemStack by lazy {
    obcClassLegacy("inventory.CraftItemStack")
}

private fun obcClassLegacy(name: String): Class<*> {
    return Class.forName("org.bukkit.craftbukkit.${Bukkit.getServer().javaClass.name.split('.')[3]}.$name")
}