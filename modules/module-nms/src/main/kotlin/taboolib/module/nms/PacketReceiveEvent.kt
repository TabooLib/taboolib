package taboolib.module.nms

import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

/**
 * TabooLib
 * taboolib.module.nms.PacketReceiveEvent
 *
 * @author sky
 * @since 2021/6/24 5:38 下午
 */
class PacketReceiveEvent(val player: Player, val packet: Packet) : BukkitProxyEvent()