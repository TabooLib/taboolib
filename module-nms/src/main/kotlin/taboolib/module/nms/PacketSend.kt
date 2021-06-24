package taboolib.module.nms

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyEvent

/**
 * TabooLib
 * taboolib.module.nms.PacketSend
 *
 * @author sky
 * @since 2021/6/24 5:38 下午
 */
class PacketSend(val player: Player, val packet: Packet) : ProxyEvent()