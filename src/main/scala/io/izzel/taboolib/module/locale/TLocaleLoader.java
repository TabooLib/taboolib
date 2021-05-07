package io.izzel.taboolib.module.locale;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.common.event.PlayerSelectLocaleEvent;
import io.izzel.taboolib.module.config.TConfigWatcher;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.module.locale.type.*;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

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

    private static final Map<String, List<String>> localePriority = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, TLocaleInstance>> map = new ConcurrentHashMap<>();

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

    /**
     * 将玩家的客户端语言转换为插件所支持的语言文件
     *
     * @param player 玩家
     * @param plugin 插件
     * @return TLocale 语言文件名称
     */
    @Nullable
    public static String playerLocaleToPluginLocale(Player player, Plugin plugin) {
        PlayerSelectLocaleEvent event;
        if (Version.isBefore(Version.v1_12)) {
            event = new PlayerSelectLocaleEvent(player, player.spigot().getLocale()).call();
        } else {
            event = new PlayerSelectLocaleEvent(player, player.getLocale()).call();
        }
        String locale = localeFormat(event.getLocale());
        for (String priority : getLocalePriority(plugin)) {
            if (priority.equalsIgnoreCase(locale)) {
                return priority;
            }
        }
        return getLocalPriorityFirst(plugin);
    }

    public static String localeFormat(String name) {
        return TabooLib.getConfig().getString("LOCALE.TRANSFER." + name.toLowerCase(), name);
    }

    @Nullable
    public static TLocaleInstance getLocaleInstance(Plugin plugin, CommandSender sender) {
        Map<String, TLocaleInstance> map = TLocaleLoader.map.get(plugin.getName());
        if (map == null) {
            return null;
        }
        String locale = null;
        if (sender instanceof Player) {
            locale = playerLocaleToPluginLocale(((Player) sender), plugin);
        } else {
            String localeTag = localeFormat(Locale.getDefault().toLanguageTag().replace("-", "_"));
            for (String priority : getLocalePriority(plugin)) {
                if (priority.equalsIgnoreCase(localeTag)) {
                    locale = priority;
                }
            }
            if (locale == null) {
                locale = getLocalPriorityFirst(plugin);
            }
        }
        return locale != null ? map.getOrDefault(locale, map.values().stream().findFirst().orElse(null)) : null;
    }

    public static void sendTo(Plugin plugin, String path, CommandSender sender, String... args) {
        TLocaleInstance instance = getLocaleInstance(plugin, sender);
        if (instance != null) {
            instance.sendTo(path, sender, args);
        }
    }

    public static String asString(Plugin plugin, String path, String... args) {
        return asString(plugin, Bukkit.getConsoleSender(), path, args);
    }

    public static String asString(Plugin plugin, CommandSender sender, String path, String... args) {
        TLocaleInstance instance = getLocaleInstance(plugin, sender);
        if (instance != null) {
            return instance.asString(path, args);
        } else {
            return "";
        }
    }

    public static List<String> asStringList(Plugin plugin, String path, String... args) {
        return asStringList(plugin, Bukkit.getConsoleSender(), path, args);
    }

    public static List<String> asStringList(Plugin plugin, CommandSender sender, String path, String... args) {
        TLocaleInstance instance = getLocaleInstance(plugin, sender);
        if (instance != null) {
            return instance.asStringList(path, args);
        } else {
            return Collections.emptyList();
        }
    }

    public static void load(Plugin plugin, boolean isCover) {
        try {
            if (isLoadLocale(plugin, isCover)) {
                for (File file : getLocaleFile(plugin)) {
                    if (TConfigWatcher.getInst().hasListener(file)) {
                        TConfigWatcher.getInst().runListener(file);
                    } else {
                        TConfigWatcher.getInst().addSimpleListener(file, () -> {
                            YamlConfiguration localeConfiguration = Files.loadYaml(file);
                            YamlConfiguration localeConfigurationAtStream = getLocaleAsPlugin(plugin, file);
                            loadPluginLocale(plugin, file, localeConfiguration, localeConfigurationAtStream);
                        }, true);
                    }
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

    public static String getLocalPriorityFirst(Plugin plugin) {
        List<String> localePriority = getLocalePriority(plugin);
        return localePriority.isEmpty() ? "zh_CN" : localePriority.get(0);
    }

    public static List<String> getLocalePriority(Plugin plugin) {
        return localePriority.getOrDefault(plugin.getName(), TabooLib.getConfig().contains("LOCALE.PRIORITY") ? TabooLib.getConfig().getStringList("LOCALE.PRIORITY") : Collections.singletonList("zh_CN"));
    }

    public static void setLocalePriority(Plugin plugin, List<String> priority) {
        localePriority.put(plugin.getName(), priority);
    }

    private static boolean isLoadLocale(Plugin plugin, boolean isCover) {
        return isCover || !isLocaleLoaded(plugin);
    }

    private static void infoLogger(String path, String... args) {
        TLogger.getGlobalLogger().info(Strings.replaceWithOrder(TabooLib.getInternal().getString(path), args));
    }

    private static void errorLogger(String... args) {
        TLogger.getGlobalLogger().error(Strings.replaceWithOrder(TabooLib.getInternal().getString("ERROR-LOADING-LANG"), args));
    }

    private static File getLocalFile(Plugin plugin, String locale) {
        List<File> localeFile = getLocaleFile(plugin);
        return localeFile.stream().filter(f -> f.getName().equals(localeFile + ".yml")).findFirst().orElse(null);
    }

    private static List<File> getLocaleFile(Plugin plugin) {
        return getLocalePriority(plugin).stream().map(file -> Files.releaseResource(plugin, "lang/" + file + ".yml")).filter(File::exists).collect(Collectors.toList());
    }

    private static TLocaleInstance createLocaleInstance(Plugin plugin, String name) {
        TLocaleInstance instance = new TLocaleInstance(plugin);
        map.computeIfAbsent(plugin.getName(), i -> new ConcurrentHashMap<>()).put(name, instance);
        return instance;
    }

    private static YamlConfiguration getLocaleAsPlugin(Plugin plugin, File localeFile) {
        try (InputStream inputStream = Files.getResourceChecked(plugin, "lang/" + localeFile.getName())) {
            if (inputStream != null) {
                return YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            Files.clearTempFiles();
        }
        return new YamlConfiguration();
    }

    private static void loadPluginLocale(Plugin plugin, File file, YamlConfiguration source, YamlConfiguration sourceAsStream) {
        TLocaleInstance instance = createLocaleInstance(plugin, file.getName().substring(0, file.getName().indexOf('.')));
        if (sourceAsStream != null) {
            instance.load(sourceAsStream);
        }
        instance.load(source);
    }
}
