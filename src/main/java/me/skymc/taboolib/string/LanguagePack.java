package me.skymc.taboolib.string;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import me.skymc.taboolib.message.MsgUtils;

@Deprecated
public class LanguagePack {
	
	private File filedir;
	private File file;
	
	private FileConfiguration fileconf;
	
	private String name;
	private Plugin plugin;
	
	private HashMap<String, List<String>> lang = new HashMap<>();
	
	public LanguagePack(String name, Plugin plugin) {
		this.plugin = plugin;
		this.name = name;
		
		filedir = new File(plugin.getDataFolder(), "Languages");
		if (!filedir.exists()) {
			filedir.mkdir();
		}
		
		file = new File(filedir, name + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				MsgUtils.Console("&8[" + plugin.getName() + "]&4 载入语言文件出错");
			}
		}
		
		fileconf = YamlConfiguration.loadConfiguration(file);
		reloadLanguage();
		
		MsgUtils.Console("&8[" + plugin.getName() + "]&7 载入语言文件&f: " + name + ".yml");
	}
	
	public File getLanguageFile() {
		return file;
	}
	
	public File getLanguageDir() {
		return filedir;
	}
	
	public FileConfiguration getLanguageConfiguration() {
		return fileconf;
	}
	
	public HashMap<String, List<String>> getLanguage() {
		return lang;
	}
	
	public String getLanguageName() {
		return name;
	}
	
	public Plugin getLanguagePlugin() {
		return plugin;
	}
	
	public void reloadLanguage(String name) {
		
		file = new File(filedir, name + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		fileconf = YamlConfiguration.loadConfiguration(file);
		reloadLanguage();
	}
	
	public void reloadLanguage() {
		lang.clear();
		
		for (String key : fileconf.getConfigurationSection("").getKeys(false)) {
			
			List<String> _lang = new ArrayList<>();
			fileconf.getStringList(key).forEach(x -> _lang.add(x.replace("&", "§")
					.replace("$plugin_name", plugin.getDescription().getName())
					.replace("$plugin_authors", plugin.getDescription().getAuthors().toString())
					.replace("$plugin_version", plugin.getDescription().getVersion())));
			
			lang.put(key, _lang);
		}
	}
}
