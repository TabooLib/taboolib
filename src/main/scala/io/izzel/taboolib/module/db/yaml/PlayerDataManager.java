package io.izzel.taboolib.module.db.yaml;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.common.event.PlayerLoadedEvent;
import io.izzel.taboolib.util.Files;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@TListener
public class PlayerDataManager implements Listener {

    private static final ConcurrentHashMap<String, FileConfiguration> PLAYER_DATA = new ConcurrentHashMap<>();

    public static UsernameType getUsernameType() {
        return TabooLib.getConfig().getBoolean("ENABLE-UUID") ? UsernameType.UUID : UsernameType.USERNAME;
    }

    public static FileConfiguration getPlayerData(Player player) {
        return getUsernameType() == UsernameType.UUID ? loadPlayerData(player.getUniqueId().toString()) : loadPlayerData(player.getName());
    }

    public static FileConfiguration loadPlayerData(String username) {
        return PLAYER_DATA.computeIfAbsent(username, n -> YamlConfiguration.loadConfiguration(Files.file(TabooLib.getInst().getPlayerDataFolder(), username + ".yml")));
    }

    public static void savePlayerData(String username, boolean remove) {
        // 没有数据
        if (!PLAYER_DATA.containsKey(username)) {
            return;
        }
        // 读取文件
        File file = Files.file(TabooLib.getInst().getPlayerDataFolder(), username + ".yml");
        // 保存配置
        try {
            PLAYER_DATA.get(username).save(file);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // 获取玩家
        Player player = getUsernameType() == UsernameType.UUID ? Bukkit.getPlayer(UUID.fromString(username)) : Bukkit.getPlayerExact(username);
        // 移除数据
        if (remove || player == null) {
            PLAYER_DATA.remove(username);
        }
    }

    public static void saveAllCaches(boolean sync, boolean remove) {
        if (sync) {
            Bukkit.getScheduler().runTaskAsynchronously(TabooLib.getPlugin(), () -> PLAYER_DATA.keySet().forEach(name -> savePlayerData(name, false)));
        } else {
            PLAYER_DATA.keySet().forEach(name -> savePlayerData(name, false));
        }
    }

    public static void saveAllPlayers(boolean sync, boolean remove) {
        if (sync) {
            Bukkit.getScheduler().runTaskAsynchronously(TabooLib.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(player -> savePlayerData(TabooLib.getConfig().getBoolean("ENABLE-UUID") ? player.getUniqueId().toString() : player.getName(), remove)));
        } else {
            Bukkit.getOnlinePlayers().forEach(player -> savePlayerData(TabooLib.getConfig().getBoolean("ENABLE-UUID") ? player.getUniqueId().toString() : player.getName(), remove));
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(TabooLib.getPlugin(), () -> {
            // 载入数据
            loadPlayerData(TabooLib.getConfig().getBoolean("ENABLE-UUID") ? e.getPlayer().getUniqueId().toString() : e.getPlayer().getName());
            // 载入完成
            Bukkit.getPluginManager().callEvent(new PlayerLoadedEvent(e.getPlayer()));
        });
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(TabooLib.getPlugin(), () -> {
            // 保存数据
            savePlayerData(TabooLib.getConfig().getBoolean("ENABLE-UUID") ? e.getPlayer().getUniqueId().toString() : e.getPlayer().getName(), true);
        });
    }

    public enum UsernameType {
        UUID, USERNAME
    }
}
