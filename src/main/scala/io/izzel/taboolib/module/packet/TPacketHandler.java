package io.izzel.taboolib.module.packet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.module.packet.channel.ChannelExecutor;
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
 * 数据包工具
 *
 * @author 坏黑
 * @since 2018-10-28 14:52
 */
@TListener
public class TPacketHandler implements Listener {

    @TInject(asm = "io.izzel.taboolib.module.packet.channel.InternalChannelExecutor")
    private static ChannelExecutor channelExecutor;
    private static final Map<String, List<TPacketListener>> packetListeners = Maps.newHashMap();

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

    /**
     * 向玩家发送数据包
     *
     * @param player 玩家
     * @param packet nms 数据包实例
     */
    public static void sendPacket(Player player, Object packet) {
        channelExecutor.sendPacket(player, packet);
    }

    /**
     * 创建数据包监听器
     *
     * @param plugin   所属插件实例
     * @param listener 监听器
     */
    public static void addListener(Plugin plugin, TPacketListener listener) {
        packetListeners.computeIfAbsent(plugin.getName(), name -> Lists.newCopyOnWriteArrayList()).add(listener);
    }

    /**
     * 移除所有数据包监听器
     *
     * @param plugin 所属插件
     */
    public static void removeListener(Plugin plugin) {
        packetListeners.remove(plugin.getName());
    }

    /**
     * 移除特定数据包监听器
     *
     * @param plugin   所属插件
     * @param listener 监听器实例
     */
    public static void removeListener(Plugin plugin, TPacketListener listener) {
        Optional.ofNullable(packetListeners.get(plugin.getName())).ifPresent(list -> list.remove(listener));
    }

    public static Collection<List<TPacketListener>> getListeners() {
        return packetListeners.values();
    }
}
