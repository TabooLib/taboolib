@file:Isolated
@file:Suppress("UNCHECKED_CAST")

package taboolib.platform.util

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import taboolib.common.Isolated
import taboolib.common.io.unzip
import taboolib.common.io.zip
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun ByteArray.deserializeToItemStack(zipped: Boolean = true): ItemStack {
    ByteArrayInputStream(if (zipped) unzip() else this).use { byteArrayInputStream ->
        BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream ->
            return bukkitObjectInputStream.readObject() as ItemStack
        }
    }
}

fun ItemStack.serializeToByteArray(zipped: Boolean = true): ByteArray {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
            bukkitObjectOutputStream.writeObject(this)
            val bytes = byteArrayOutputStream.toByteArray()
            return if (zipped) bytes.zip() else bytes
        }
    }
}

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