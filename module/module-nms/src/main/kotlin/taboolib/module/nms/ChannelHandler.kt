@file:Suppress("DEPRECATION")

package taboolib.module.nms

import io.netty.channel.ChannelHandlerAdapter
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
class ChannelHandler(val player: Player) : ChannelHandlerAdapter() {

    override fun write(channelHandlerContext: ChannelHandlerContext, packet: Any, channelPromise: ChannelPromise) {
        if (PacketSendEvent(player, Packet(packet)).call()) {
            super.write(channelHandlerContext, packet, channelPromise)
        }
    }

    override fun channelRead(channelHandlerContext: ChannelHandlerContext, packet: Any) {
        if (PacketReceiveEvent(player, Packet(packet)).call()) {
            super.channelRead(channelHandlerContext, packet)
        }
    }
}