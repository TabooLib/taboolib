package com.ilummc.tlib.resources;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.annotations.TLocalePlugin;
import com.ilummc.tlib.logger.TLogger;
import com.ilummc.tlib.resources.type.*;
import com.ilummc.tlib.util.IO;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.other.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TLocaleLoader {

    private static final Map<String, TLocaleInstance> map = new ConcurrentHashMap<>();

    public static void init() {
        ConfigurationSerialization.registerClass(TLocaleText.class, "TEXT");
        ConfigurationSerialization.registerClass(TLocaleJson.class, "JSON");
        ConfigurationSerialization.registerClass(TLocaleSound.class, "SOUND");
        ConfigurationSerialization.registerClass(TLocaleTitle.class, "TITLE");
        ConfigurationSerialization.registerClass(TLocaleActionBar.class, "ACTION");
    }

    public static void sendTo(Plugin plugin, String path, CommandSender sender, String... args) {
        if (Bukkit.isPrimaryThread()) {
            Optional.ofNullable(map.get(plugin.getName())).ifPresent(localeInstance -> localeInstance.sendTo(path, sender, args));
        } else {
            synchronized (TLocaleLoader.class) {
                Optional.ofNullable(map.get(plugin.getName())).ifPresent(localeInstance -> localeInstance.sendTo(path, sender, args));
            }
        }
    }

    public static String asString(Plugin plugin, String path, String... args) {
        TLocaleInstance tLocaleInstance = map.get(plugin.getName());
        if (tLocaleInstance != null) {
            return tLocaleInstance.asString(path, args);
        } else {
            return "";
        }
    }

    public static List<String> asStringList(Plugin plugin, String path, String... args) {
        TLocaleInstance tLocaleInstance = map.get(plugin.getName());
        if (tLocaleInstance != null) {
            return tLocaleInstance.asStringList(path, args);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 载入语言文件
     *
     * @param plugin  载入插件
     * @param isCover 是否覆盖
     */
    public static void load(Plugin plugin, boolean isCover) {
        try {
            if (isLoadLocale(plugin, isCover)) {
                // 获取文件
                File localeFile = getLocaleFile(plugin);
                if (localeFile == null) {
                    return;
                }

                // 加载文件
                infoLogger("TRY-LOADING-LANG", plugin.getName(), localeFile.getName());
                YamlConfiguration localeConfiguration = ConfigUtils.loadYaml(plugin, localeFile);
                YamlConfiguration localeConfigurationAtStream = getLocaleAtStream(plugin, localeFile);

                // 载入配置
                loadPluginLocale(plugin, localeFile, localeConfiguration, localeConfigurationAtStream);

                // 注册监听
                TLib.getTLib().getConfigWatcher().removeListener(localeFile);
                TLib.getTLib().getConfigWatcher().addListener(localeFile, null, obj -> {
                    infoLogger("RELOADING-LANG", plugin.getName());
                    loadPluginLocale(plugin, localeFile, ConfigUtils.loadYaml(plugin, localeFile), getLocaleAtStream(plugin, localeFile));
                });
            }
        } catch (Exception e) {
            errorLogger("ERROR-LOADING-LANG", plugin.getName(), e.toString() + "\n" + e.getStackTrace()[0].toString());
        }
    }

    private static boolean isLoadLocale(Plugin plugin, boolean isCover) {
        return (isCover || !isLocaleLoaded(plugin)) && (plugin.equals(Main.getInst()) || isDependWithTabooLib(plugin));
    }

    private static void infoLogger(String path, String... args) {
        TLogger.getGlobalLogger().info(Strings.replaceWithOrder(TLib.getInternalLanguage().getString(path), args));
    }

    private static void errorLogger(String path, String... args) {
        TLogger.getGlobalLogger().error(Strings.replaceWithOrder(TLib.getInternalLanguage().getString(path), args));
    }

    private static boolean isVersionOutOfDate(YamlConfiguration configuration, YamlConfiguration configurationAtSteam) {
        return configurationAtSteam != null && configurationAtSteam.getDouble("VERSION", 0) > configuration.getDouble("VERSION", 0);
    }

    private static File getLocaleFile(Plugin plugin) {
        releaseLocales(plugin);
        return getLocalePriority().stream().map(localeName -> new File(plugin.getDataFolder(), "lang/" + localeName + ".yml")).filter(File::exists).findFirst().orElse(null);
    }

    private static void releaseLocales(Plugin plugin) {
        getLocalePriority().stream().filter(localeName -> !new File(plugin.getDataFolder(), "lang/" + localeName + ".yml").exists() && plugin.getResource("lang/" + localeName + ".yml") != null).forEach(localeName -> plugin.saveResource("lang/" + localeName + ".yml", true));
    }

    public static boolean isLocaleLoaded(Plugin plugin) {
        return map.containsKey(plugin.getName());
    }

    public static boolean isDependWithTabooLib(Plugin plugin) {
        return plugin.getClass().getAnnotation(TLocalePlugin.class) != null || plugin.getDescription().getDepend().contains(Main.getInst().getName()) || plugin.getDescription().getSoftDepend().contains(Main.getInst().getName());
    }

    public static List<String> getLocalePriority() {
        return Main.getInst().getConfig().contains("LOCALE.PRIORITY") ? Main.getInst().getConfig().getStringList("LOCALE.PRIORITY") : Collections.singletonList("zh_CN");
    }

    private static TLocaleInstance getLocaleInstance(Plugin plugin) {
        TLocaleInstance instance = new TLocaleInstance(plugin);
        map.put(plugin.getName(), instance);
        return instance;
    }

    private static YamlConfiguration getLocaleAtStream(Plugin plugin, File localeFile) {
        InputStream localeInputSteam = plugin.getClass().getResourceAsStream("/lang/" + localeFile.getName());
        try {
            String yamlText = new String(IO.readFully(localeInputSteam), Charset.forName("utf-8"));
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(yamlText);
            return yaml;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void loadPluginLocale(Plugin plugin, File localeFile, YamlConfiguration localeConfiguration, YamlConfiguration localeConfigurationAtStream) {
        TLocaleInstance localeInstance = getLocaleInstance(plugin);
        boolean versionOutOfDate = isVersionOutOfDate(localeConfiguration, localeConfigurationAtStream);
        if (versionOutOfDate) {
            localeInstance.load(localeConfigurationAtStream);
        }
        localeInstance.load(localeConfiguration);
        if (!versionOutOfDate || localeInstance.size() - localeInstance.getUpdateNodes() == 0) {
            infoLogger("SUCCESS-LOADING-LANG-NORMAL", plugin.getName(), localeFile.getName().split("\\.")[0], String.valueOf(localeInstance.size()));
        } else {
            infoLogger("SUCCESS-LOADING-LANG-UPDATE", plugin.getName(), localeFile.getName().split("\\.")[0], String.valueOf(localeInstance.size()), String.valueOf(localeInstance.size() - localeInstance.getUpdateNodes()));
        }
    }
}
