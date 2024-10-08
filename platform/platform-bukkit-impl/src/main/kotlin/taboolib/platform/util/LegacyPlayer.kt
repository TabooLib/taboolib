package taboolib.platform.util

import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common.util.unsafeLazy

/**
 * 老版本反射工具，无需支持 Paper 1.20.6+
 *
 * @author 坏黑
 * @since 2024/7/10 17:34
 */
@Suppress("HasPlatformType")
object LegacyPlayer {

    val legacyVersion by unsafeLazy {
        Bukkit.getServer().javaClass.name.split('.')[3]
    }

    val rChatCompoundText by unsafeLazy {
        nmsClass("ChatComponentText").getDeclaredConstructor(String::class.java)
    }

    val rPacketPlayOutTitle by unsafeLazy {
        nmsClass("PacketPlayOutTitle").getDeclaredConstructor()
    }

    val rEnumTitleAction by unsafeLazy {
        nmsClass("PacketPlayOutTitle\$EnumTitleAction").enumConstants
    }

    val rPacketPlayOutChat by unsafeLazy {
        nmsClass("PacketPlayOutChat").getDeclaredConstructor()
    }

    fun nmsClass(name: String): Class<*> {
        return Class.forName("net.minecraft.server.$legacyVersion.$name")
    }

    fun sendTitle(player: Player, title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        val connection = player.getProperty<Any>("entity/playerConnection")!!
        connection.invokeMethod<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
            it.setProperty("a", rEnumTitleAction[4])
        })
        connection.invokeMethod<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
            it.setProperty("a", rEnumTitleAction[2])
            it.setProperty("c", fadein)
            it.setProperty("d", stay)
            it.setProperty("e", fadeout)
        })
        if (title != null) {
            connection.invokeMethod<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
                it.setProperty("a", rEnumTitleAction[0])
                it.setProperty("b", rChatCompoundText.newInstance(title))
            })
        }
        if (subtitle != null) {
            connection.invokeMethod<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
                it.setProperty("a", rEnumTitleAction[1])
                it.setProperty("b", rChatCompoundText.newInstance(subtitle))
            })
        }
    }

    fun sendActionBar(player: Player, message: String) {
        player.getProperty<Any>("entity/playerConnection")!!.invokeMethod<Void>("sendPacket", rPacketPlayOutChat.newInstance().also {
            it.setProperty("b", 2.toByte())
            it.setProperty("components", TextComponent.fromLegacyText(message))
        })
    }
}