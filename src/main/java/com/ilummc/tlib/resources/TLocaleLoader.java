package com.ilummc.tlib.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.inject.TConfigInjector;
import com.ilummc.tlib.resources.type.TLocaleText;
import com.ilummc.tlib.resources.type.TLocaleTitle;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.fileutils.FileUtils;

public class TLocaleLoader {

    private static final Map<String, TLocaleInstance> map = new HashMap<>();
    
    static void sendTo(Plugin plugin, String path, CommandSender sender, String... args) {
        Optional.ofNullable(map.get(plugin.getName())).ifPresent(localeInstance -> localeInstance.sendTo(path, sender, args));
    }

    static String asString(Plugin plugin, String path) {
    	return map.get(plugin.getName()).asString(path);
    }

    public static void init() {
        ConfigurationSerialization.registerClass(TLocaleText.class, "TEXT");
        ConfigurationSerialization.registerClass(TLocaleTitle.class, "TITLE");
    }

    public static void load(Plugin plugin, boolean ignoreLoaded) {
        try {
            if ((!ignoreLoaded || !map.containsKey(plugin.getName())) && plugin == Main.getInst() || plugin.getDescription().getDepend().contains("TabooLib") || plugin.getDescription().getSoftDepend().contains("TabooLib")) {
                InputStream inputStream = null;
                File file = null;
                String lang = null;
                for (String s : TLib.getTLib().getConfig().getLocale()) {
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
                	TLib.getTLib().getLogger().error("语言文件加载失败");
                	return;
                }
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    saveResource(inputStream, file);
                }
                TLib.getTLib().getLogger().info("尝试加载 " + lang + ".yml 作为语言文件");
                YamlConfiguration configuration = ConfigUtils.loadYaml(plugin, file);
                TLocaleInstance localeInstance = new TLocaleInstance();
                localeInstance.load(configuration);
                map.put(plugin.getName(), localeInstance);
                TConfigInjector.fixUnicode(configuration);
                TLib.getTLib().getLogger().info("成功加载 " + lang + " 语言文件");
            }
        } catch (Exception e) {
            TLib.getTLib().getLogger().error("载入语言文件发生异常：" + e.toString());
        }
    }
    
    private static void saveResource(InputStream inputStream, File file) {
    	FileOutputStream fileOutputStream = null;
    	try {
    		byte[] data = FileUtils.read(inputStream);
    		fileOutputStream = new FileOutputStream(file);
    		fileOutputStream.write(data);
    	} catch (Exception e) {
			TLib.getTLib().getLogger().error("资源文件写入失败: " + file);
			TLib.getTLib().getLogger().error("原因: " + e.getMessage());
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (Exception ignored) {
			}
		}
    }
}
