@file:Isolated
package taboolib.platform.util

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.Isolated
import taboolib.common.util.unsafeLazy
import taboolib.module.chat.ComponentText
import taboolib.module.chat.RawMessage

fun ItemStack.toNMSKeyAndItemData(): Pair<String, String> {
    val nmsItemStack = classCraftItemStack.invokeMethod<Any>("asNMSCopy", this, isStatic = true)!!
    val nmsKey = try {
        type.key.key
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
    return nmsKey to (nmsItemStack.invokeMethod<Any>("getTag")?.toString() ?: "{}")
}

fun ComponentText.hoverItem(itemStack: ItemStack): ComponentText {
    val (key, data) = itemStack.toNMSKeyAndItemData()
    return hoverItem(key, data)
}

fun RawMessage.hoverItem(itemStack: ItemStack): RawMessage {
    val (key, data) = itemStack.toNMSKeyAndItemData()
    return hoverItem(key, data)
}

private val classCraftItemStack by unsafeLazy {
    obcClassLegacy("inventory.CraftItemStack")
}

private fun obcClassLegacy(name: String): Class<*> {
    return Class.forName("org.bukkit.craftbukkit.${Bukkit.getServer().javaClass.name.split('.')[3]}.$name")
}