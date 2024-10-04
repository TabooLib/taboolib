@file:Suppress("UNCHECKED_CAST")

package taboolib.platform.util

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import taboolib.common.io.unzip
import taboolib.common.io.zip
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * 将字节数组反序列化为 ItemStack 对象。
 *
 * @param zipped 指示字节数组是否被压缩，默认为 true。
 * @return 反序列化后的 ItemStack 对象。
 */
fun ByteArray.deserializeToItemStack(zipped: Boolean = true): ItemStack {
    ByteArrayInputStream(if (zipped) unzip() else this).use { byteArrayInputStream ->
        BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream ->
            return bukkitObjectInputStream.readObject() as ItemStack
        }
    }
}

/**
 * 将 ItemStack 对象序列化为字节数组。
 *
 * @param zipped 指示是否压缩序列化后的字节数组，默认为 true。
 * @return 序列化后的字节数组。
 */
fun ItemStack.serializeToByteArray(zipped: Boolean = true): ByteArray {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
            bukkitObjectOutputStream.writeObject(this)
            val bytes = byteArrayOutputStream.toByteArray()
            return if (zipped) bytes.zip() else bytes
        }
    }
}

/**
 * 将字节数组反序列化为 Inventory 对象。
 *
 * @param inventory 可选参数，用于指定要填充的现有 Inventory 对象。如果为 null，将创建新的 Inventory。
 * @param zipped 指示字节数组是否被压缩，默认为 true。
 * @return 反序列化后的 Inventory 对象。
 */
fun ByteArray.deserializeToInventory(inventory: Inventory? = null, zipped: Boolean = true): Inventory {
    ByteArrayInputStream(if (zipped) unzip() else this).use { byteArrayInputStream ->
        BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream ->
            val items = bukkitObjectInputStream.readObject() as List<Int>
            val size = bukkitObjectInputStream.readInt()
            val inv = inventory ?: Bukkit.createInventory(null, size)
            items.forEach { inv.setItem(it, bukkitObjectInputStream.readObject() as ItemStack) }
            return inv
        }
    }
}

/**
 * 将 Inventory 对象序列化为字节数组。
 *
 * @param size 指定要序列化的物品栏大小，默认为当前物品栏的大小。
 * @param zipped 指示是否压缩序列化后的字节数组，默认为 true。
 * @return 序列化后的字节数组。
 */
fun Inventory.serializeToByteArray(size: Int = this.size, zipped: Boolean = true): ByteArray {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
            val items = (0 until size).map { it to getItem(it) }.filter { it.second.isNotAir() }.toMap()
            bukkitObjectOutputStream.writeObject(items.keys.toList())
            bukkitObjectOutputStream.writeInt(size)
            items.forEach { (_, v) -> bukkitObjectOutputStream.writeObject(v) }
            val bytes = byteArrayOutputStream.toByteArray()
            return if (zipped) bytes.zip() else bytes
        }
    }
}