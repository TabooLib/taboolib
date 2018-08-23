package me.skymc.taboolib.playerdata;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.database.PlayerDataManager;
import me.skymc.taboolib.exception.PlayerOfflineException;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.listener.TListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@TListener
public class DataUtils implements Listener {

    public static final ConcurrentHashMap<String, HashMap<String, FileConfiguration>> CACHE_DATA_PLUGIN = new ConcurrentHashMap<>();

    public static void saveAllCaches(Plugin plugin) {
        saveAllCaches(plugin, false);
    }

    public static void saveAllCaches(Plugin plugin, boolean remove) {
        if (plugin == null || !CACHE_DATA_PLUGIN.containsKey(plugin.getName())) {
            return;
        }
        for (String fileName : CACHE_DATA_PLUGIN.get(plugin.getName()).keySet()) {
            saveConfiguration(CACHE_DATA_PLUGIN.get(plugin.getName()).get(fileName), FileUtils.file(getDataSaveFolder(plugin), fileName));
        }
        if (remove) {
            CACHE_DATA_PLUGIN.remove(plugin.getName());
        }
    }

    public static void saveAllCaches() {
        saveAllCaches(false);
    }

    public static void saveAllCaches(boolean remove) {
        long time = System.currentTimeMillis();
        for (String plugin : CACHE_DATA_PLUGIN.keySet()) {
            saveAllCaches(getFixedPlugin(plugin), remove);
        }
        if (!Main.getInst().getConfig().getBoolean("HIDE-NOTIFY")) {
            TLocale.Logger.info("DATA-UTILS.SUCCESS-SAVE-DATA", String.valueOf(DataUtils.CACHE_DATA_PLUGIN.size()), String.valueOf(System.currentTimeMillis() - time));
        }
    }

    public static void saveConfiguration(FileConfiguration conf, File file) {
        try {
            conf.save(file);
        } catch (IOException e) {
            TLocale.Logger.error("DATA-UTILS.FAIL-SAVE-FILE", file.getName(), e.toString());
        }
    }

    public static String getFixedFileName(String name) {
        return name.contains(".") ? name : name + ".yml";
    }

    public static Plugin getFixedPlugin(String pluginName) {
        return Bukkit.getPluginManager().getPlugin(pluginName) == null ? Main.getInst() : Bukkit.getPluginManager().getPlugin(pluginName);
    }

    public static File getDataSaveFolder(Plugin plugin) {
        return plugin == null || plugin.equals(Main.getInst()) ? Main.getServerDataFolder() : plugin.getDataFolder();
    }

    public static String getDataSaveKey(Plugin plugin) {
        return plugin == null || plugin.equals(Main.getInst()) ? Main.getInst().getName() : plugin.getName();
    }

    public static FileConfiguration addPluginData(String name, Plugin plugin) {
        return setPluginData(getFixedFileName(name), plugin, YamlConfiguration.loadConfiguration(FileUtils.file(getDataSaveFolder(plugin), getFixedFileName(name))));
    }

    public static FileConfiguration getPluginData(String name, Plugin plugin) {
        return !CACHE_DATA_PLUGIN.containsKey(getDataSaveKey(plugin)) ? new YamlConfiguration() : CACHE_DATA_PLUGIN.get(getDataSaveKey(plugin)).get(getFixedFileName(name));
    }

    public static FileConfiguration setPluginData(String name, Plugin plugin, FileConfiguration conf) {
        if (!CACHE_DATA_PLUGIN.containsKey(getDataSaveKey(plugin))) {
            CACHE_DATA_PLUGIN.put(getDataSaveKey(plugin), new HashMap<>());
        }
        CACHE_DATA_PLUGIN.get(getDataSaveKey(plugin)).put(getFixedFileName(name), conf);
        return conf;
    }

    @Deprecated
    public static FileConfiguration getPlayerData(String name) {
        try {
            return PlayerDataManager.getPlayerData(name, true);
        } catch (PlayerOfflineException e) {
            return new YamlConfiguration();
        }
    }

    @Deprecated
    public static FileConfiguration addPlayerData(String name) {
        return PlayerDataManager.loadPlayerData(name);
    }

    @Deprecated
    public static void savePlayerData(String name, boolean remove) {
        PlayerDataManager.savePlayerData(name, remove);
    }

    @Deprecated
    public static FileConfiguration registerServerData(String name) {
        return addPluginData(name, null);
    }

    @Deprecated
    public static FileConfiguration getPlayerData(OfflinePlayer p) {
        return getPlayerData(p.getName());
    }

    @Deprecated
    public static void setPlayerData(OfflinePlayer p, String s, Object o) {
        getPlayerData(p.getName()).set(s, o);
    }

    @Deprecated
    public static void saveData(OfflinePlayer p) {
        saveOnline(p.getName());
    }

    public static Long getOnline(OfflinePlayer p) {
        return getPlayerData(p).getLong("TabooLib.Offline");
    }

    public static void saveOnline(String p) {
        getPlayerData(p).set("TabooLib.Offline", System.currentTimeMillis());
    }

    @EventHandler
    public void disable(PluginDisableEvent e) {
        if (e.getPlugin().equals(Main.getInst())) {
            return;
        }
        if (CACHE_DATA_PLUGIN.containsKey(e.getPlugin().getName())) {
            saveAllCaches(e.getPlugin(), true);
        }
    }
}
