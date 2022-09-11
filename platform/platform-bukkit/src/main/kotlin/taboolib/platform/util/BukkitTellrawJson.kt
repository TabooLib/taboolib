@file:Isolated
package taboolib.platform.util

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import taboolib.common.Isolated
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.util.unsafeLazy
import taboolib.module.chat.TellrawJson

fun TellrawJson.hoverItem(itemStack: ItemStack): TellrawJson {
    val nmsItemStack = classCraftItemStack.invokeMethod<Any>("asNMSCopy", itemStack, isStatic = true)!!
    val nmsKey = try {
        itemStack.type.key.key
    } catch (ex: NoSuchMethodError) {
        val nmsItem = nmsItemStack.invokeMethod<Any>("getItem")!!
        val name = nmsItem.getProperty<String>("name")!!
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
    return hoverItem(nmsKey, nmsItemStack.invokeMethod<Any>("getTag")?.toString() ?: "{}")
}

private val classCraftItemStack by unsafeLazy {
    obcClassLegacy("inventory.CraftItemStack")
}

private fun obcClassLegacy(name: String): Class<*> {
    return Class.forName("org.bukkit.craftbukkit.${Bukkit.getServer().javaClass.name.split('.')[3]}.$name")
}