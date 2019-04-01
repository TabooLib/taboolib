package me.skymc.taboolib.common.packet.channel;

import com.ilummc.tlib.logger.TLogger;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @Author 坏黑
 * @Since 2018-10-28 15:12
 */
public class InternalChannelExecutor extends ChannelExecutor {

    @Override
    public void sendPacket(Player player, Object packet) {
        if (packet instanceof Packet) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet) packet);
        } else {
            TLogger.getGlobalLogger().warn("Invalid packet: " + packet.getClass().getName());
        }
    }

    @Override
    public Channel getPlayerChannel(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
    }
}
