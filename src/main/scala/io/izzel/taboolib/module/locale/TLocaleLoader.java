package io.izzel.taboolib.module.locale;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.common.plugin.InternalPlugin;
import io.izzel.taboolib.module.config.TConfigWatcher;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.module.locale.type.*;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 语言文件加载工具
 */
public class TLocaleLoader {

    private static final Map<String, List<String>> localePriority = new HashMap<>();
    private static final Map<String, TLocaleInstance> map = new ConcurrentHashMap<>();

    static {
        // 插件版载入 > 非插件版（导致非插件版语言文件类型被覆盖）
        // 解决方案：识别插件版语言文件类型转换为非插件版
        // 发现于： 2019年7月13日
        // 非插件版载入 > 插件版（导致插件版语言文件类型被覆盖）
        // 解决方案：检测插件版是否已经被加载
        // 发现于： 2019年7月14日
        if (!TabooLibAPI.isOriginLoaded()) {
            ConfigurationSerialization.registerClass(TLocaleText.class, "TEXT");
            ConfigurationSerialization.registerClass(TLocaleJson.class, "JSON");
            ConfigurationSerialization.registerClass(TLocaleBook.class, "BOOK");
            ConfigurationSerialization.registerClass(TLocaleSound.class, "SOUND");
            ConfigurationSerialization.registerClass(TLocaleTitle.class, "TITLE");
            ConfigurationSerialization.registerClass(TLocaleBossBar.class, "BAR");
            ConfigurationSerialization.registerClass(TLocaleActionBar.class, "ACTION");
        }
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

    public static void load(Plugin plugin, boolean isCover) {
        load(plugin, isCover, true);
    }

    public static void load(Plugin plugin, boolean isCover, boolean hideMessage) {
        try {
            if (isLoadLocale(plugin, isCover)) {
                for (File localeFile : getLocaleFile(plugin)) {
                    if (!TConfigWatcher.getInst().hasListener(localeFile)) {
                        Runnable listener = () -> {
                            if (localeFile.getName().equals(getLocalPriorityFirst(plugin) + ".yml")) {
                                YamlConfiguration localeConfiguration = Files.loadYaml(localeFile);
                                YamlConfiguration localeConfigurationAtStream = getLocaleAsPlugin(plugin, localeFile);
                                loadPluginLocale(plugin, localeFile, localeConfiguration, localeConfigurationAtStream, hideMessage);
                            }
                        };
                        TConfigWatcher.getInst().addListener(localeFile, null, obj -> listener.run());
                    }
                    TConfigWatcher.getInst().runListener(localeFile);
                }
            }
        } catch (Exception e) {
            errorLogger(plugin.getName(), e.toString() + "\n" + e.getStackTrace()[0].toString());
        }
    }

    public static void unload(Plugin plugin) {
        map.remove(plugin.getName());
    }

    public static boolean isLocaleLoaded(Plugin plugin) {
        return map.containsKey(plugin.getName());
    }

    public static boolean isDependWithTabooLib(Plugin plugin) {
        return plugin.getClass().getSuperclass().getSimpleName().equals("TabooPlugin");
    }

    public static String getLocalPriorityFirst(Plugin plugin) {
        List<String> localePriority = getLocalePriority(plugin);
        return localePriority.isEmpty() ? "zh_CN" : localePriority.get(0);
    }

    public static List<String> getLocalePriority(Plugin plugin) {
        return localePriority.getOrDefault(plugin.getName(), TabooLib.getConfig().contains("LOCALE.PRIORITY") ? TabooLib.getConfig().getStringList("LOCALE.PRIORITY") : Collections.singletonList("zh_CN"));
    }

    public static List<String> setLocalePriority(Plugin plugin, List<String> priority) {
        return localePriority.put(plugin.getName(), priority);
    }

    private static boolean isLoadLocale(Plugin plugin, boolean isCover) {
        return isCover || !isLocaleLoaded(plugin);
    }

    private static void infoLogger(String path, String... args) {
        TLogger.getGlobalLogger().info(Strings.replaceWithOrder(TabooLib.getInst().getInternal().getString(path), args));
    }

    private static void errorLogger(String... args) {
        TLogger.getGlobalLogger().error(Strings.replaceWithOrder(TabooLib.getInst().getInternal().getString("ERROR-LOADING-LANG"), args));
    }

    private static File getLocalFile(Plugin plugin, String locale) {
        List<File> localeFile = getLocaleFile(plugin);
        return localeFile.stream().filter(f -> f.getName().equals(localeFile + ".yml")).findFirst().orElse(null);
    }

    private static List<File> getLocaleFile(Plugin plugin) {
        return getLocalePriority(plugin).stream().map(file -> Files.releaseResource(plugin, "lang/" + file + ".yml")).filter(File::exists).collect(Collectors.toList());
    }

    private static TLocaleInstance getLocaleInstance(Plugin plugin) {
        TLocaleInstance instance = new TLocaleInstance(plugin);
        map.put(plugin.getName(), instance);
        return instance;
    }

    private static YamlConfiguration getLocaleAsPlugin(Plugin plugin, File localeFile) {
        try (InputStream canonicalResource = Files.getCanonicalResource(plugin, (plugin instanceof InternalPlugin ? "__resources__/" : "") + "lang/" + localeFile.getName())) {
            if (canonicalResource != null) {
                return YamlConfiguration.loadConfiguration(new InputStreamReader(canonicalResource, StandardCharsets.UTF_8));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            Files.clearTempFiles();
        }
        return new YamlConfiguration();
    }

    private static void loadPluginLocale(Plugin plugin, File localeFile, YamlConfiguration localeConfiguration, YamlConfiguration localeConfigurationAtStream, boolean hideMessage) {
        TLocaleInstance localeInstance = getLocaleInstance(plugin);
        if (localeConfigurationAtStream != null) {
            localeInstance.load(localeConfigurationAtStream);
        }
        localeInstance.load(localeConfiguration);
        if (hideMessage) {
            return;
        }
        if (localeInstance.getLatestUpdateNodes().get() <= 0) {
            infoLogger("SUCCESS-LOADING-LANG-NORMAL", plugin.getName(), localeFile.getName().split("\\.")[0], String.valueOf(localeInstance.size()));
        } else {
            infoLogger("SUCCESS-LOADING-LANG-UPDATE", plugin.getName(), localeFile.getName().split("\\.")[0], String.valueOf(localeInstance.size()), String.valueOf(localeInstance.getLatestUpdateNodes().get()));
        }
    }
}
