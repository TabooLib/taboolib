package com.ilummc.tlib.resources;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.inject.TConfigInjector;
import me.skymc.taboolib.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LocaleLoader {

    private static final Map<String, LocaleInstance> map = new HashMap<>();

    static void sendTo(Plugin plugin, String path, CommandSender sender, String... args) {
        Optional.ofNullable(map.get(plugin.getName())).ifPresent(localeInstance -> localeInstance.sendTo(path, sender, args));
    }

    static void sendTo(Plugin plugin, String path, CommandSender sender) {
        Optional.ofNullable(map.get(plugin.getName())).ifPresent(localeInstance -> localeInstance.sendTo(path, sender));
    }

    public static void init() {
        ConfigurationSerialization.registerClass(SimpleChatMessage.class);
        ConfigurationSerialization.registerClass(SimpleChatMessage.class, "Message");
        ConfigurationSerialization.registerClass(SimpleChatMessage.class, "MESSAGE");
        ConfigurationSerialization.registerClass(SimpleChatMessage.class, "TEXT");
        ConfigurationSerialization.registerClass(SimpleChatMessage.class, "Text");
    }

    public static void load(Plugin plugin, boolean ignoreLoaded) {
        try {
            if ((!ignoreLoaded || !map.containsKey(plugin.getName())) && plugin == Main.getInst() ||
                    plugin.getDescription().getDepend().contains("TabooLib") || plugin.getDescription().getSoftDepend().contains("TabooLib")) {
                InputStream inputStream = null;
                File file = null;
                String lang = null;
                for (String s : TLib.getTLib().getConfig().getLocale()) {
                    lang = s;
                    file = new File(plugin.getDataFolder(), "/lang/" + s + ".yml");
                    if (file.exists()) {
                        inputStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
                        break;
                    } else if ((inputStream = plugin.getClass().getResourceAsStream("/lang/" + s + ".yml")) != null)
                        break;
                }
                if (inputStream == null) return;
                TLib.getTLib().getLogger().info("尝试加载 " + lang + ".yml 作为语言文件");
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, Charset.forName("utf-8")));
                LocaleInstance localeInstance = new LocaleInstance();
                localeInstance.load(configuration);
                map.put(plugin.getName(), localeInstance);
                TConfigInjector.fixUnicode(configuration);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                configuration.save(file);
                TLib.getTLib().getLogger().info("成功加载 " + lang + " 语言文件");
            }
        } catch (Exception e) {
            TLib.getTLib().getLogger().error("载入语言文件发生异常：" + e.toString());
        }
    }

}
