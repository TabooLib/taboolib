package taboolib.module.nms

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.chat.ComponentSerializer
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.server.v1_16_R3.IChatBaseComponent.ChatSerializer
import net.minecraft.server.v1_16_R3.PacketPlayOutTitle
import net.minecraft.server.v1_9_R1.PacketPlayOutChat
import org.bukkit.boss.BossBar
import org.bukkit.craftbukkit.v1_16_R3.boss.CraftBossBar
import org.bukkit.entity.Player
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common.util.unsafeLazy

fun BossBar.setRawTitle(title: String) {
    NMSMessage.instance.setTitle(this, title)
}

fun Player.sendRawTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
    NMSMessage.instance.send(this, title, subtitle, fadein, stay, fadeout)
}

fun Player.sendRawActionBar(message: String) {
    NMSMessage.instance.send(this, message)
}

abstract class NMSMessage {

    abstract fun send(player: Player, title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int)

    abstract fun send(player: Player, action: String)

    abstract fun setTitle(bossBar: BossBar, title: String)

    companion object {

        val instance by unsafeLazy { nmsProxy<NMSMessage>() }
    }
}

class NMSMessageImpl : NMSMessage() {

    override fun send(player: Player, title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        if (MinecraftVersion.isUniversal) {
            player.sendPacket(ClientboundSetTitlesAnimationPacket(fadein, stay, fadeout))
            if (title != null) {
                player.sendPacket(ClientboundSetTitleTextPacket(IChatBaseComponent.ChatSerializer.fromJson(title)))
            }
            if (subtitle != null) {
                player.sendPacket(ClientboundSetSubtitleTextPacket(IChatBaseComponent.ChatSerializer.fromJson(subtitle)))
            }
        } else {
            player.sendPacket(PacketPlayOutTitle(fadein, stay, fadeout))
            if (title != null) {
                player.sendPacket(PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, ChatSerializer.a(title)))
            }
            if (subtitle != null) {
                player.sendPacket(PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, ChatSerializer.a(subtitle)))
            }
        }
    }

    override fun send(player: Player, action: String) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *ComponentSerializer.parse(action))
        } catch (ex: NoSuchMethodError) {
            player.sendPacket(PacketPlayOutChat().also {
                it.setProperty("b", 2.toByte())
                it.setProperty("components", ComponentSerializer.parse(action))
            })
        }
    }

    override fun setTitle(bossBar: BossBar, title: String) {
        bossBar as CraftBossBar
        bossBar.handle.a(ChatSerializer.a(title))
    }
}