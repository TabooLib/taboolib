@file:Isolated
package taboolib.platform.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import taboolib.common.Isolated
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common.util.unsafeLazy
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.TellrawJson

/**
 * 构建一本书
 */
fun buildBook(builder: BookBuilder.() -> Unit = {}): ItemStack {
    return BookBuilder().also(builder).build()
}

/**
 * 构建一本书并发送给玩家
 */
fun Player.sendBook(builder: BookBuilder.() -> Unit = {}) {
    sendBook(buildBook(builder))
}

/**
 * 发送一本书给玩家
 */
fun Player.sendBook(itemStack: ItemStack) {
    try {
        invokeMethod<Void>("openBook", itemStack)
    } catch (ex: NoSuchMethodException) {
        val itemInHand = itemInHand
        setItemInHand(itemStack)
        try {
            val nmsItemStack = classCraftItemStack.invokeMethod<Any>("asNMSCopy", itemStack, isStatic = true)
            val handle = getProperty<Any>("entity")!!
            try {
                handle.invokeMethod<Void>("a", nmsItemStack, enumHandMainHand)
            } catch (ex: NoSuchMethodException) {
                handle.invokeMethod<Void>("openBook", nmsItemStack, enumHandMainHand)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        setItemInHand(itemInHand)
    }
}

private val isUniversal = try {
    nmsClassLegacy("IChatBaseComponent\$ChatSerializer")
    false
} catch (ignored: ClassNotFoundException) {
    true
}

private val classCraftItemStack by unsafeLazy {
    obcClassLegacy("inventory.CraftItemStack")
}

private val classChatSerializer by unsafeLazy {
    nmsClassLegacy("IChatBaseComponent\$ChatSerializer")
}

private val enumHandMainHand by unsafeLazy {
    nmsClassLegacy("EnumHand").enumConstants[0]
}

private fun obcClassLegacy(name: String): Class<*> {
    return Class.forName("org.bukkit.craftbukkit.${Bukkit.getServer().javaClass.name.split('.')[3]}.$name")
}

private fun nmsClassLegacy(name: String): Class<*> {
    return Class.forName("net.minecraft.server.${Bukkit.getServer().javaClass.name.split('.')[3]}.$name")
}

@Isolated
open class BookBuilder : ItemBuilder(XMaterial.WRITTEN_BOOK) {

    class Text(val text: String, val raw: Boolean = false)

    var title = "untitled"
    var author = "untitled"

    val bookPages = ArrayList<Text>()

    fun write(text: String) {
        bookPages += Text(text)
    }

    fun write(text: TellrawJson) {
        writeRaw(text.toRawMessage())
    }

    fun writeRaw(text: String) {
        bookPages += Text(text, raw = true)
    }

    override fun build(): ItemStack {
        return super.build().modifyMeta<BookMeta> {
            title = "untitled"
            author = "untitled"
            bookPages.forEach {
                if (it.raw) {
                    var pages = getProperty<MutableList<Any>>("pages")
                    if (pages == null) {
                        pages = ArrayList()
                        setProperty("pages", pages)
                    }
                    try {
                        getProperty<Boolean>("resolved")
                        pages += it.text
                    } catch (ex: NoSuchFieldException) {
                        pages += classChatSerializer.invokeMethod<Any>("a", it.text, isStatic = true)!!
                    }
                } else {
                    addPage(it.text)
                }
            }
        }
    }
}