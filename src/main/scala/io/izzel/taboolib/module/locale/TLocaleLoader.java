package io.izzel.taboolib.module.locale;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.module.locale.type.*;
import io.izzel.taboolib.module.config.TConfigWatcher;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.IO;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TLocaleLoader {

    private static final Map<String, TLocaleInstance> map = new ConcurrentHashMap<>();

    static {
        ConfigurationSerialization.registerClass(TLocaleText.class, "TEXT");
        ConfigurationSerialization.registerClass(TLocaleJson.class, "JSON");
        ConfigurationSerialization.registerClass(TLocaleBook.class, "BOOK");
        ConfigurationSerialization.registerClass(TLocaleSound.class, "SOUND");
        ConfigurationSerialization.registerClass(TLocaleTitle.class, "TITLE");
        ConfigurationSerialization.registerClass(TLocaleBossBar.class, "BAR");
        ConfigurationSerialization.registerClass(TLocaleActionBar.class, "ACTION");
    }

    public static void sendTo(Plugin plugin, String path, CommandSender sender, String... args) {
        TabooLibAPI.debug(plugin, "TLocaleLoader.sendTo: " + plugin + ", path: " + path + ", sender: " + sender + ", args: " + Arrays.asList(args));
        if (Bukkit.isPrimaryThread()) {
            Optional.ofNullable(map.get(plugin.getName())).ifPresent(localeInstance -> localeInstance.sendTo(path, sender, args));
        } else {
            synchronized (TLocaleLoader.class) {
                Optional.ofNullable(map.get(plugin.getName())).ifPresent(localeInstance -> localeInstance.sendTo(path, sender, args));
            }
        }
    }

    public static String asString(Plugin plugin, String path, String... args) {
        TabooLibAPI.debug(plugin, "TLocaleLoader.asString: " + plugin.getName() + ", path: " + path + ", args: " + Arrays.asList(args));
        TLocaleInstance tLocaleInstance = map.get(plugin.getName());
        if (tLocaleInstance != null) {
            return tLocaleInstance.asString(path, args);
        } else {
            return "";
        }
    }

    public static List<String> asStringList(Plugin plugin, String path, String... args) {
        TabooLibAPI.debug(plugin, "TLocaleLoader.asStringList: " + plugin + ", path: " + path + ", args: " + Arrays.asList(args));
        TLocaleInstance tLocaleInstance = map.get(plugin.getName());
        if (tLocaleInstance != null) {
            return tLocaleInstance.asStringList(path, args);
        } else {
            return Collections.emptyList();
        }
    }

    public static void load(Plugin plugin, boolean isCover) {
        try {
            if (isLoadLocale(plugin, isCover)) {
                // 获取文件
                File localeFile = getLocaleFile(plugin);
                if (localeFile == null) {
                    return;
                }
                // 加载文件
                YamlConfiguration localeConfiguration = Files.loadYaml(localeFile);
                YamlConfiguration localeConfigurationAtStream = getLocaleAtStream(plugin, localeFile);
                // 载入配置
                loadPluginLocale(plugin, localeFile, localeConfiguration, localeConfigurationAtStream);
                // 注册监听
                TConfigWatcher.getInst().removeListener(localeFile);
                TConfigWatcher.getInst().addListener(localeFile, null, obj -> loadPluginLocale(plugin, localeFile, Files.loadYaml(localeFile), getLocaleAtStream(plugin, localeFile)));
            }
        } catch (Exception e) {
            errorLogger("ERROR-LOADING-LANG", plugin.getName(), e.toString() + "\n" + e.getStackTrace()[0].toString());
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

    public static List<String> getLocalePriority() {
        return TabooLib.getConfig().contains("LOCALE.PRIORITY") ? TabooLib.getConfig().getStringList("LOCALE.PRIORITY") : Collections.singletonList("zh_CN");
    }

    private static boolean isLoadLocale(Plugin plugin, boolean isCover) {
        return isCover || !isLocaleLoaded(plugin);
    }

    private static void infoLogger(String path, String... args) {
        TLogger.getGlobalLogger().info(Strings.replaceWithOrder(io.izzel.taboolib.TabooLib.getInst().getInternal().getString(path), args));
    }

    private static void errorLogger(String path, String... args) {
        TLogger.getGlobalLogger().error(Strings.replaceWithOrder(io.izzel.taboolib.TabooLib.getInst().getInternal().getString(path), args));
    }

    private static File getLocaleFile(Plugin plugin) {
        releaseLocales(plugin);
        return getLocalePriority().stream().map(localeName -> new File("plugins/" + plugin.getName() + "/lang/" + localeName + ".yml")).filter(File::exists).findFirst().orElse(null);
    }

    private static void releaseLocales(Plugin plugin) {
        getLocalePriority().stream().filter(localeName -> !new File("plugins/" + plugin.getName() + "/lang/" + localeName + ".yml").exists() && plugin.getResource("lang/" + localeName + ".yml") != null).forEach(localeName -> plugin.saveResource("lang/" + localeName + ".yml", true));
    }

    private static TLocaleInstance getLocaleInstance(Plugin plugin) {
        TLocaleInstance instance = new TLocaleInstance(plugin);
        map.put(plugin.getName(), instance);
        return instance;
    }

    private static YamlConfiguration getLocaleAtStream(Plugin plugin, File localeFile) {
        InputStream localeInputSteam = Files.getResource(plugin, "lang/" + localeFile.getName());
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(IO.readFully(localeInputSteam, Charset.forName("utf-8")));
            return yaml;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void loadPluginLocale(Plugin plugin, File localeFile, YamlConfiguration localeConfiguration, YamlConfiguration localeConfigurationAtStream) {
        TLocaleInstance localeInstance = getLocaleInstance(plugin);
        if (localeConfigurationAtStream != null) {
            localeInstance.load(localeConfigurationAtStream);
        }
        localeInstance.load(localeConfiguration);
        if (localeInstance.getLatestUpdateNodes().get() <= 0) {
            infoLogger("SUCCESS-LOADING-LANG-NORMAL", plugin.getName(), localeFile.getName().split("\\.")[0], String.valueOf(localeInstance.size()));
        } else {
            infoLogger("SUCCESS-LOADING-LANG-UPDATE", plugin.getName(), localeFile.getName().split("\\.")[0], String.valueOf(localeInstance.size()), String.valueOf(localeInstance.getLatestUpdateNodes().get()));
        }
    }
}
