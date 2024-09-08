package taboolib.module.nms

import io.netty.channel.Channel
import org.bukkit.entity.Player
import taboolib.common.event.CancelableInternalEvent

/**
 * TabooLib
 * taboolib.module.nms.PacketReceiveEvent
 *
 * @author sky
 * @since 2021/6/24 5:38 下午
 */
class PacketReceiveEvent(val player: Player, val packet: Packet) : CancelableInternalEvent() {

    /**
     * for early login/status packets
     */
    class Handshake(val channel: Channel, val packet: Packet) : CancelableInternalEvent()
}