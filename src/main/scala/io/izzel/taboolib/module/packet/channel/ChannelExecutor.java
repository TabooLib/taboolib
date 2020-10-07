package io.izzel.taboolib.module.packet.channel;

import io.izzel.taboolib.module.packet.Packet;
import io.izzel.taboolib.module.packet.TPacketHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author 坏黑
 * @Since 2018-10-28 14:34
 */
public abstract class ChannelExecutor {

    private final ExecutorService addChannelService = Executors.newSingleThreadExecutor();
    private final ExecutorService removeChannelService = Executors.newSingleThreadExecutor();

    public abstract void sendPacket(Player player, Object packet);

    public abstract Channel getPlayerChannel(Player player);

    public void addPlayerChannel(Player player) {
        addChannelService.submit(() -> {
            getPlayerChannel(player).pipeline().addBefore("packet_handler", "taboolib5_packet_handler", new ChannelHandler(player));
        });
    }

    public void removePlayerChannel(Player player) {
        removeChannelService.submit(() -> {
            Channel playerChannel = getPlayerChannel(player);
            if (playerChannel.pipeline().get("taboolib5_packet_handler") != null) {
                playerChannel.pipeline().remove("taboolib5_packet_handler");
            }
        });
    }

    static class ChannelHandler extends ChannelDuplexHandler {

        private final Player player;

        public ChannelHandler(Player player) {
            this.player = player;
        }

        @Override
        public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) throws Exception {
            try {
                if (TPacketHandler.getListeners().stream().flatMap(Collection::stream).anyMatch(packetListener -> !packetListener.onSend(player, o) || !packetListener.onSend(player, new Packet(o)))) {
                    return;
                }
            } catch (ConcurrentModificationException ignore) {
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.write(channelHandlerContext, o, channelPromise);
        }

        @Override
        public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
            try {
                if (TPacketHandler.getListeners().stream().flatMap(Collection::stream).anyMatch(packetListener -> !packetListener.onReceive(player, o) || !packetListener.onReceive(player, new Packet(o)))) {
                    return;
                }
            } catch (ConcurrentModificationException ignore) {
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.channelRead(channelHandlerContext, o);
        }
    }
}
