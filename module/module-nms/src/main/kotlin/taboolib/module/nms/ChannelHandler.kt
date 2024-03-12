package taboolib.module.nms

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import org.bukkit.entity.Player

/**
 * TabooLib
 * taboolib.module.nms.ChannelHandler
 *
 * @author sky
 * @since 2021/6/24 5:42 下午
 */
class ChannelHandler(val player: Player) : ChannelDuplexHandler() {

    override fun write(channelHandlerContext: ChannelHandlerContext, packet: Any, channelPromise: ChannelPromise) {
        val event = PacketSendEvent(player, PacketImpl(packet))
        if (event.callIf()) {
            super.write(channelHandlerContext, event.packet.source, channelPromise)
        }
    }

    override fun channelRead(channelHandlerContext: ChannelHandlerContext, packet: Any) {
        val event = PacketReceiveEvent(player, PacketImpl(packet))
        if (event.callIf()) {
            super.channelRead(channelHandlerContext, event.packet.source)
        }
    }
}