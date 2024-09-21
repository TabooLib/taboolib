package taboolib.module.nms;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * TabooLib
 * taboolib.module.nms.LightInjectorImpl
 *
 * @author 坏黑
 * @since 2024/7/20 12:54
 */
public class LightInjectorImpl extends LightInjector {

    /**
     * Initializes the injector and starts to listen to packets.
     * <p>
     * Note that, while it is possible to create more than one instance per plugin,
     * it's more efficient and recommended to just have only one.
     *
     * @param plugin The {@link Plugin} which is instantiating this injector.
     * @throws NullPointerException     If the provided {@code plugin} is {@code null}.
     * @throws IllegalStateException    When <b>not</b> called from the main thread.
     * @throws IllegalArgumentException If the provided {@code plugin} is not enabled.
     */
    public LightInjectorImpl(@NotNull Plugin plugin) {
        super(plugin);
    }

    @Override
    protected @Nullable Object onPacketReceiveAsync(@Nullable Player sender, @NotNull Channel channel, @NotNull Object packet) {
        if (sender != null) {
            PacketReceiveEvent event = new PacketReceiveEvent(sender, new PacketImpl(packet));
            if (event.callIf()) {
                return ProtocolHandler.INSTANCE.handlePacket(ProtocolHandler.PACKET_RECEIVE, sender, channel, event.getPacket().getSource());
            } else {
                return null;
            }
        } else {
            PacketReceiveEvent.Handshake event = new PacketReceiveEvent.Handshake(channel, new PacketImpl(packet));
            if (event.callIf()) {
                return ProtocolHandler.INSTANCE.handlePacket(ProtocolHandler.PACKET_RECEIVE, null, channel, event.getPacket().getSource());
            } else {
                return null;
            }
        }
    }

    @Override
    protected @Nullable Object onPacketSendAsync(@Nullable Player receiver, @NotNull Channel channel, @NotNull Object packet) {
        if (receiver != null) {
            PacketSendEvent event = new PacketSendEvent(receiver, new PacketImpl(packet));
            if (event.callIf()) {
                return ProtocolHandler.INSTANCE.handlePacket(ProtocolHandler.PACKET_SEND, receiver, channel, event.getPacket().getSource());
            } else {
                return null;
            }
        } else {
            PacketSendEvent.Handshake event = new PacketSendEvent.Handshake(channel, new PacketImpl(packet));
            if (event.callIf()) {
                return ProtocolHandler.INSTANCE.handlePacket(ProtocolHandler.PACKET_SEND, null, channel, event.getPacket().getSource());
            } else {
                return null;
            }
        }
    }
}
