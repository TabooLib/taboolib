@file:Internal
package taboolib.internal

import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import java.lang.reflect.Constructor

internal val legacyVersion by lazy {
    Bukkit.getServer().javaClass.name.split('.')[3]
}

internal val rChatCompoundText: Constructor<*> by lazy {
    nmsClass("ChatComponentText").getDeclaredConstructor(String::class.java)
}

internal val rPacketPlayOutTitle: Constructor<*> by lazy {
    nmsClass("PacketPlayOutTitle").getDeclaredConstructor()
}

internal val rEnumTitleAction: Array<*> by lazy {
    nmsClass("PacketPlayOutTitle\$EnumTitleAction").enumConstants
}

internal val rPacketPlayOutChat: Constructor<*> by lazy {
    nmsClass("PacketPlayOutChat").getDeclaredConstructor()
}

internal fun nmsClass(name: String): Class<*> {
    return Class.forName("net.minecraft.server.$legacyVersion.$name")
}

internal fun Player.sendTitleLegacy(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
    val connection = getProperty<Any>("entity/playerConnection")!!
    if (title != null) {
        connection.invokeMethod<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
            it.setProperty("a", rEnumTitleAction[0])
            it.setProperty("b", rChatCompoundText.newInstance(title))
            it.setProperty("c", fadein)
            it.setProperty("d", stay)
            it.setProperty("e", fadeout)
        })
    }
    if (subtitle != null) {
        connection.invokeMethod<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
            it.setProperty("a", rEnumTitleAction[1])
            it.setProperty("b", rChatCompoundText.newInstance(subtitle))
            it.setProperty("c", fadein)
            it.setProperty("d", stay)
            it.setProperty("e", fadeout)
        })
    }
    connection.invokeMethod<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
        it.setProperty("a", rEnumTitleAction[3])
        it.setProperty("c", fadein)
        it.setProperty("d", stay)
        it.setProperty("e", fadeout)
    })
}

internal fun Player.sendActionBarLegacy(message: String) {
    getProperty<Any>("entity/playerConnection")!!.invokeMethod<Void>("sendPacket", rPacketPlayOutChat.newInstance().also {
        it.setProperty("b", 2.toByte())
        it.setProperty("components", TextComponent.fromLegacyText(message))
    })
}