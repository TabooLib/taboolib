package me.skymc.taboolib.string;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import me.skymc.taboolib.message.MsgUtils;

@Deprecated
public class LanguageUtils {
	
	public static String a(LanguagePack l, String key) {
		if (l.getLanguage().containsKey(key)) {
			return l.getLanguage().get(key).get(0);
		}
		return "";
	}
	
	public static List<String> b(LanguagePack l, String key) {
		if (l.getLanguage().containsKey(key)) {
			return l.getLanguage().get(key);
		}
		return Arrays.asList("");
	}
	
	public static void saveLanguageFile(String name, Plugin plugin) {
		if (!new File(new File(plugin.getDataFolder(), "Languages"), name + ".yml").exists()) {
			plugin.saveResource("Languages/" + name + ".yml", true);
			MsgUtils.Console("&8[" + plugin.getName() + "]&7 生成语言文件&f: " + name + ".yml");
		}
	}
}
