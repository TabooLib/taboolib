package taboolib.module.nms.v2

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.nmsProxy
import taboolib.module.nms.sendPacket

/**
 * 将 Json 信息设置到 [BossBar] 的标题栏
 */
fun BossBar.setRawTitle(title: String) {
    nmsProxy<NMSMessage>().setRawTitle(this, title)
}

/**
 * 发送 Json 信息到玩家的标题栏
 */
fun Player.sendRawTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
    nmsProxy<NMSMessage>().sendRawTitle(this, title, subtitle, fadein, stay, fadeout)
}

/**
 * 发送 Json 信息到玩家的动作栏
 */
fun Player.sendRawActionBar(message: String) {
    nmsProxy<NMSMessage>().sendRawActionBar(this, message)
}

/**
 * TabooLib
 * taboolib.module.nms.NMSMessage
 *
 * @author 坏黑
 * @since 2023/8/5 03:47
 */
abstract class NMSMessage {

    abstract fun setRawTitle(bossBar: BossBar, title: String)

    abstract fun sendRawTitle(player: Player, title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int)

    abstract fun sendRawActionBar(player: Player, action: String)
}

/**
 * [NMSMessage] 的实现类
 */
class NMSMessageImpl : NMSMessage() {

    override fun setRawTitle(bossBar: BossBar, title: String) {
        bossBar as CraftBossBar16
        bossBar.handle.a(NMSChatSerializer16.a(title))
    }

    override fun sendRawTitle(player: Player, title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        if (MinecraftVersion.isUniversal) {
            player.sendPacket(NMSClientboundSetTitlesAnimationPacket(fadein, stay, fadeout))
            if (title != null) {
                player.sendPacket(NMSClientboundSetTitleTextPacket(NMSChatSerializer.fromJson(title)))
            }
            if (subtitle != null) {
                player.sendPacket(NMSClientboundSetSubtitleTextPacket(NMSChatSerializer.fromJson(subtitle)))
            }
        } else {
            player.sendPacket(NMSPacketPlayOutTitle16(fadein, stay, fadeout))
            if (title != null) {
                player.sendPacket(NMSPacketPlayOutTitle16(NMSEnumTitleAction16.TITLE, NMSChatSerializer16.a(title)))
            }
            if (subtitle != null) {
                player.sendPacket(NMSPacketPlayOutTitle16(NMSEnumTitleAction16.SUBTITLE, NMSChatSerializer16.a(subtitle)))
            }
        }
    }

    override fun sendRawActionBar(player: Player, action: String) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *ComponentSerializer.parse(action))
        } catch (ex: NoSuchMethodError) {
            player.sendPacket(NMSPacketPlayOutChat16().also {
                it.setProperty("b", 2.toByte())
                it.setProperty("components", ComponentSerializer.parse(action))
            })
        }
    }
}

private typealias NMSIChatBaseComponent = net.minecraft.network.chat.IChatBaseComponent
private typealias NMSChatSerializer = net.minecraft.network.chat.IChatBaseComponent.ChatSerializer
private typealias NMSClientboundSetSubtitleTextPacket = net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
private typealias NMSClientboundSetTitleTextPacket = net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
private typealias NMSClientboundSetTitlesAnimationPacket = net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
private typealias NMSChatSerializer16 = net.minecraft.server.v1_16_R3.IChatBaseComponent.ChatSerializer
private typealias NMSPacketPlayOutTitle16 = net.minecraft.server.v1_16_R3.PacketPlayOutTitle
private typealias NMSEnumTitleAction16 = net.minecraft.server.v1_16_R3.PacketPlayOutTitle.EnumTitleAction
private typealias NMSPacketPlayOutChat16 = net.minecraft.server.v1_16_R3.PacketPlayOutChat
private typealias CraftBossBar16 = org.bukkit.craftbukkit.v1_16_R3.boss.CraftBossBar