package com.ilummc.tlib.resources;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.resources.type.TLocaleActionBar;
import com.ilummc.tlib.resources.type.TLocaleJson;
import com.ilummc.tlib.resources.type.TLocaleText;
import com.ilummc.tlib.resources.type.TLocaleTitle;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.fileutils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TLocaleLoader {

    private static final Map<String, TLocaleInstance> map = new ConcurrentHashMap<>();

    static void sendTo(Plugin plugin, String path, CommandSender sender, String... args) {
        if (Bukkit.isPrimaryThread())
            Optional.ofNullable(map.get(plugin.getName())).ifPresent(localeInstance -> localeInstance.sendTo(path, sender, args));
        else synchronized (TLocaleLoader.class) {
            Optional.ofNullable(map.get(plugin.getName())).ifPresent(localeInstance -> localeInstance.sendTo(path, sender, args));
        }
    }

    static String asString(Plugin plugin, String path, String... args) {
        return map.get(plugin.getName()).asString(path, args);
    }

    public static void init() {
        ConfigurationSerialization.registerClass(TLocaleText.class, "TEXT");
        ConfigurationSerialization.registerClass(TLocaleTitle.class, "TITLE");
        ConfigurationSerialization.registerClass(TLocaleJson.class, "JSON");
        ConfigurationSerialization.registerClass(TLocaleActionBar.class, "ACTION");
    }

    public static void load(Plugin plugin, boolean ignoreLoaded) {
        try {
            if ((!ignoreLoaded || !map.containsKey(plugin.getName())) && (plugin == Main.getInst() || plugin.getDescription().getDepend().contains("TabooLib") || plugin.getDescription().getSoftDepend().contains("TabooLib"))) {
                InputStream inputStream = null;
                File file = null;
                String lang = null;
                for (String s : Main.getInst().getConfig().getStringList("LOCALE.PRIORITY")) {
                    lang = s;
                    file = new File(plugin.getDataFolder(), "/lang/" + s + ".yml");
                    if (file.exists()) {
                        inputStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
                        break;
                    } else if ((inputStream = plugin.getClass().getResourceAsStream("/lang/" + s + ".yml")) != null) {
                        break;
                    }
                }
                if (inputStream == null) {
                    TLib.getTLib().getLogger().error(TLib.getTLib().getInternalLang().getString("LANG-LOAD-FAIL"));
                    return;
                }
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    plugin.saveResource("lang/" + lang + ".yml", true);
                }
                TLib.getTLib().getLogger().info(Strings.replaceWithOrder(TLib.getTLib().getInternalLang().getString("TRY-LOADING-LANG"), plugin.getName(), lang));
                {
                    YamlConfiguration configuration = ConfigUtils.loadYaml(plugin, file);
                    TLocaleInstance localeInstance = new TLocaleInstance(plugin);
                    localeInstance.load(configuration);
                    map.put(plugin.getName(), localeInstance);
                    TLib.getTLib().getLogger().info(Strings.replaceWithOrder(TLib.getTLib().getInternalLang().getString("SUCCESS-LOADING-LANG"),
                            plugin.getName(), lang, String.valueOf(localeInstance.size())));
                }
                File finalFile = file;
                String finalLang = lang;
                TLib.getTLib().getConfigWatcher().addListener(file, null, obj -> {
                    TLib.getTLib().getLogger().info(Strings.replaceWithOrder(TLib.getTLib().getInternalLang().getString("RELOADING-LANG"), plugin.getName()));
                    YamlConfiguration configuration = ConfigUtils.loadYaml(plugin, finalFile);
                    TLocaleInstance localeInstance = new TLocaleInstance(plugin);
                    localeInstance.load(configuration);
                    map.put(plugin.getName(), localeInstance);
                    TLib.getTLib().getLogger().info(Strings.replaceWithOrder(TLib.getTLib().getInternalLang().getString("SUCCESS-LOADING-LANG"),
                            plugin.getName(), finalLang, String.valueOf(localeInstance.size())));
                });
            }
        } catch (Exception e) {
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getTLib().getInternalLang().getString("ERROR-LOADING-LANG"),
                    plugin.getName(), e.toString() + "\n" + e.getStackTrace()[0].toString()));
        }
    }

}
