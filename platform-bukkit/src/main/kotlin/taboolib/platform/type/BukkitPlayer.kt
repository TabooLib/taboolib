package taboolib.platform.type

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.common.reflect.Reflex.Companion.reflexInvoke
import taboolib.common.util.Location
import taboolib.platform.util.dispatchCommand
import taboolib.platform.util.toBukkitLocation
import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.platform.type.BukkitPlayer
 *
 * @author sky
 * @since 2021/6/17 10:33 下午
 */
class BukkitPlayer(val player: Player) : ProxyPlayer {

    val legacyVersion by lazy {
        Bukkit.getServer().javaClass.name.split('.')[3]
    }

    val rChatCompoundText by lazy {
        nmsClass("ChatComponentText").getDeclaredConstructor(String::class.java)
    }

    val rPacketPlayOutTitle by lazy {
        nmsClass("PacketPlayOutTitle").getDeclaredConstructor()
    }

    val rEnumTitleAction by lazy {
        nmsClass("PacketPlayOutTitle\$EnumTitleAction").enumConstants
    }

    fun nmsClass(name: String): Class<*> {
        return Class.forName("net.minecraft.server.$legacyVersion.$name")
    }

    override val origin: Any
        get() = player

    override val name: String
        get() = player.name

    override val address: InetSocketAddress?
        get() = player.address

    override val uniqueId: UUID
        get() = player.uniqueId

    override val ping: Int
        get() = player.reflex<Int>("ping")!!

    override val locale: String
        get() = player.locale

    override val world: String
        get() = player.world.name

    override val location: Location
        get() = Location(world, player.location.x, player.location.y, player.location.z, player.location.yaw, player.location.pitch)

    override fun kick(message: String?) {
        player.kickPlayer(message)
    }

    override fun chat(message: String) {
        player.chat(message)
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(location.toBukkitLocation(), Sound.valueOf(sound), volume, pitch)
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(location.toBukkitLocation(), sound, volume, pitch)
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        try {
            player.sendTitle(title, subtitle, fadein, stay, fadeout)
        } catch (ex: NoSuchMethodError) {
            val connection = player.reflexInvoke<Any>("getHandle")!!.reflex<Any>("playerConnection")!!
            if (title != null) {
                connection.reflexInvoke<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
                    it.reflex("a", rEnumTitleAction[0])
                    it.reflex("b", rChatCompoundText.newInstance(title))
                    it.reflex("c", fadein)
                    it.reflex("d", stay)
                    it.reflex("e", fadeout)
                })
            }
            if (subtitle != null) {
                connection.reflexInvoke<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
                    it.reflex("a", rEnumTitleAction[1])
                    it.reflex("b", rChatCompoundText.newInstance(subtitle))
                    it.reflex("c", fadein)
                    it.reflex("d", stay)
                    it.reflex("e", fadeout)
                })
            }
            connection.reflexInvoke<Void>("sendPacket", rPacketPlayOutTitle.newInstance().also {
                it.reflex("a", rEnumTitleAction[3])
                it.reflex("c", fadein)
                it.reflex("d", stay)
                it.reflex("e", fadeout)
            })
        }
    }

    override fun sendActionBar(message: String) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message)[0])
    }

    override fun sendRawMessage(message: String) {
        player.sendRawMessage(message)
    }

    override fun sendMessage(message: String) {
        player.sendMessage(message)
    }

    override fun performCommand(command: String): Boolean {
        return dispatchCommand(player, command)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun teleport(loc: Location) {
        player.teleport(taboolib.common.util.Location(Bukkit.getWorld(loc.world!!), loc.x, loc.y, loc.z, loc.yaw, loc.pitch))
    }
}