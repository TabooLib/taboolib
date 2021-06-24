package taboolib.module.nms

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyEvent

/**
 * TabooLib
 * taboolib.module.nms.PacketReceive
 *
 * @author sky
 * @since 2021/6/24 5:38 下午
 */
class PacketReceive(val player: Player, val packet: Packet) : ProxyEvent()