package taboolib.module.nms

import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

/**
 * TabooLib
 * taboolib.module.nms.PacketSendEvent
 *
 * @author sky
 * @since 2021/6/24 5:38 下午
 */
class PacketSendEvent(val player: Player, val packet: Packet) : BukkitProxyEvent()