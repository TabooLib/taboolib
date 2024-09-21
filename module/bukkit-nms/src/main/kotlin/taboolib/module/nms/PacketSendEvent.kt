package taboolib.module.nms

import io.netty.channel.Channel
import org.bukkit.entity.Player
import taboolib.common.event.CancelableInternalEvent

/**
 * 数据包发送事件，可用于拦截数据包
 * 自 6.2 版本起，不能放行已被拦截的数据包（即使用 isCancelled = false）
 */
class PacketSendEvent(val player: Player, val packet: Packet) : CancelableInternalEvent() {

    /**
     * for early login/status packets
     */
    class Handshake(val channel: Channel, val packet: Packet) : CancelableInternalEvent()
}