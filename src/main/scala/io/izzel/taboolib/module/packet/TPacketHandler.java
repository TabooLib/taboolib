package io.izzel.taboolib.module.packet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.taboolib.module.packet.channel.ChannelExecutor;
import io.izzel.taboolib.module.lite.SimpleVersionControl;
import io.izzel.taboolib.module.inject.TListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author 坏黑
 * @Since 2018-10-28 14:52
 */
@TListener
public class TPacketHandler implements Listener {

    private static Map<String, List<TPacketListener>> packetListeners = Maps.newHashMap();
    private static ChannelExecutor channelExecutor;

    public TPacketHandler() {
        try {
            channelExecutor = (ChannelExecutor) SimpleVersionControl.createNMS("io.izzel.taboolib.module.packet.channel.InternalChannelExecutor").translate().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        channelExecutor.addPlayerChannel(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        channelExecutor.removePlayerChannel(e.getPlayer());
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        removeListener(e.getPlugin());
    }

    public static void sendPacket(Player player, Object packet) {
        channelExecutor.sendPacket(player, packet);
    }

    public static void addListener(Plugin plugin, TPacketListener listener) {
        packetListeners.computeIfAbsent(plugin.getName(), name -> Lists.newArrayList()).add(listener);
    }

    public static void removeListener(Plugin plugin) {
        packetListeners.remove(plugin.getName());
    }

    public static void removeListener(Plugin plugin, TPacketListener listener) {
        Optional.ofNullable(packetListeners.get(plugin.getName())).ifPresent(list -> list.remove(listener));
    }

    public static Collection<List<TPacketListener>> getListeners() {
        return packetListeners.values();
    }
}
