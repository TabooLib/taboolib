package taboolib.module.nms.v2

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
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.nmsProxy
import taboolib.module.nms.sendPacket

/**
 * 将 Json 信息设置到 [BossBar] 的标题上
 */
fun BossBar.setRawTitle(title: String) {
    nmsProxy<NMSMessage>().setRawTitle(this, title)
}

/**
 * 发送 Json 信息到玩家的 Title 上
 */
fun Player.sendRawTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
    nmsProxy<NMSMessage>().sendRawTitle(this, title, subtitle, fadein, stay, fadeout)
}

/**
 * 发送 Json 信息到玩家的 ActionBar 上
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
        bossBar as CraftBossBar
        bossBar.handle.a(ChatSerializer.a(title))
    }

    override fun sendRawTitle(player: Player, title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
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

    override fun sendRawActionBar(player: Player, action: String) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *ComponentSerializer.parse(action))
        } catch (ex: NoSuchMethodError) {
            player.sendPacket(PacketPlayOutChat().also {
                it.setProperty("b", 2.toByte())
                it.setProperty("components", ComponentSerializer.parse(action))
            })
        }
    }
}