package me.skymc.taboolib.cloud;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.cloud.expansion.Expansion;
import me.skymc.taboolib.cloud.expansion.ExpansionType;
import me.skymc.taboolib.common.function.TFunction;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.plugin.PluginUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Map;

/**
 * @Author sky
 * @Since 2018-09-30 17:30
 */
@TFunction
public class TCloudLoader {

    private static String url = "https://gitee.com/bkm016/TabooLibCloud/raw/master/cloud.json";
    private static String latestJsonOrigin;
    private static JsonObject latestJsonObject;
    private static Map<String, Expansion> expansionPlugins = Maps.newHashMap();
    private static Map<String, Expansion> expansionInternal = Maps.newHashMap();
    private static File expansionInternalFolder;

    public void onEnable() {
        createFolder();
        refresh();
    }

    public static void createFolder() {
        if (!(expansionInternalFolder = new File(TabooLib.instance().getDataFolder(), "TCloud")).exists()) {
            expansionInternalFolder.mkdirs();
        }
    }

    public static void refresh() {
        Bukkit.getScheduler().runTaskAsynchronously(TabooLib.instance(), () -> {
            long time = System.currentTimeMillis();
            latestJsonOrigin = FileUtils.getStringFromURL(url, 1024);
            if (latestJsonOrigin == null) {
                TLocale.Logger.error("TCLOUD.LIST-CONNECT-FAILED");
                return;
            }
            TLocale.Logger.info("TCLOUD.LIST-CONNECT-SUCCESS", String.valueOf(System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
            try {
                latestJsonObject = new JsonParser().parse(latestJsonOrigin).getAsJsonObject();
            } catch (Exception e) {
                TLocale.Logger.info("TCLOUD.LIST-PARSE-FAILED", e.getMessage());
                return;
            }
            TLocale.Logger.info("TCLOUD.LIST-PARSE-SUCCESS", String.valueOf(System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
            if (latestJsonObject.has("plugins")) {
                for (Map.Entry<String, JsonElement> pluginEntry : latestJsonObject.getAsJsonObject("plugins").entrySet()) {
                    try {
                        expansionPlugins.put(pluginEntry.getKey(), Expansion.unSerialize(ExpansionType.PLUGIN, pluginEntry.getKey(), pluginEntry.getValue().getAsJsonObject()));
                    } catch (Exception e) {
                        TLocale.Logger.info("TCLOUD.LIST-LOAD-FAILED", pluginEntry.getKey(), e.getMessage());
                    }
                }
            }
            if (latestJsonObject.has("internal")) {
                for (Map.Entry<String, JsonElement> pluginEntry : latestJsonObject.getAsJsonObject("internal").entrySet()) {
                    try {
                        expansionInternal.put(pluginEntry.getKey(), Expansion.unSerialize(ExpansionType.INTERNAL, pluginEntry.getKey(), pluginEntry.getValue().getAsJsonObject()));
                    } catch (Exception e) {
                        TLocale.Logger.info("TCLOUD.LIST-LOAD-FAILED", pluginEntry.getKey(), e.getMessage());
                    }
                }
            }
            TLocale.Logger.info("TCLOUD.LIST-LOAD-SUCCESS", String.valueOf(System.currentTimeMillis() - time));
        });
    }

    public static boolean isConnected() {
        return latestJsonOrigin != null;
    }

    public static String getLatestJsonOrigin() {
        return latestJsonOrigin;
    }

    public static JsonObject getLatestJsonObject() {
        return latestJsonObject;
    }

    public static Map<String, Expansion> getExpansionPlugins() {
        return expansionPlugins;
    }

    public static Map<String, Expansion> getExpansionInternal() {
        return expansionInternal;
    }

    public static File getExpansionInternalFolder() {
        return expansionInternalFolder;
    }

    public static Expansion getExpansion(String name) {
        return expansionPlugins.getOrDefault(name, expansionInternal.get(name));
    }

    public static boolean isExpansionExists(Expansion expansion) {
        return expansion.getType() == ExpansionType.PLUGIN && PluginUtils.isPluginExists(expansion.getName());
    }
}
