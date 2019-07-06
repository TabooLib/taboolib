package io.izzel.taboolib.module.db.yaml;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.util.Files;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class PluginDataManager {

    public static final ConcurrentHashMap<String, HashMap<String, FileConfiguration>> CACHE_DATA_PLUGIN = new ConcurrentHashMap<>();

    public static void saveAllCaches(Plugin plugin) {
        saveAllCaches(plugin, false);
    }

    public static void saveAllCaches(Plugin plugin, boolean remove) {
        if (plugin == null || !CACHE_DATA_PLUGIN.containsKey(plugin.getName())) {
            return;
        }
        for (String fileName : CACHE_DATA_PLUGIN.get(plugin.getName()).keySet()) {
            saveConfiguration(CACHE_DATA_PLUGIN.get(plugin.getName()).get(fileName), Files.file(getDataSaveFolder(plugin), fileName));
        }
        if (remove) {
            CACHE_DATA_PLUGIN.remove(plugin.getName());
        }
    }

    public static void saveAllCaches() {
        saveAllCaches(false);
    }

    public static void saveAllCaches(boolean remove) {
        CACHE_DATA_PLUGIN.keySet().forEach(plugin -> saveAllCaches(getFixedPlugin(plugin), remove));
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
        return Bukkit.getPluginManager().getPlugin(pluginName) == null ? TabooLib.getPlugin() : Bukkit.getPluginManager().getPlugin(pluginName);
    }

    public static File getDataSaveFolder(Plugin plugin) {
        return plugin == null || plugin.getName().equals("TabooLib") ? TabooLib.getInst().getServerDataFolder() : plugin.getDataFolder();
    }

    public static String getDataSaveKey(Plugin plugin) {
        return plugin == null ? "TabooLib" : plugin.getName();
    }

    public static FileConfiguration addPluginData(String name, Plugin plugin) {
        return setPluginData(getFixedFileName(name), plugin, YamlConfiguration.loadConfiguration(Files.file(getDataSaveFolder(plugin), getFixedFileName(name))));
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
}
