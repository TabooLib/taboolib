package me.skymc.taboolib.fileutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.google.common.base.Charsets;
import com.ilummc.tlib.TLib;

public class ConfigUtils {
	
	public static FileConfiguration decodeYAML(String args) {
		return YamlConfiguration.loadConfiguration(new StringReader(Base64Coder.decodeString(args)));
	}
	
	public static String encodeYAML(FileConfiguration file) {
		return Base64Coder.encodeLines(file.saveToString().getBytes()).replaceAll("\\s+", "");
	}
	
	/**
	 * 以 UTF-8 的格式释放配置文件并载入
	 * 
	 * 录入时间：2018年2月10日21:28:30
	 * 录入版本：3.49
	 * 
	 * @param plugin
	 * @return
	 */
	public static FileConfiguration saveDefaultConfig(Plugin plugin, String name) {
		File file = new File(plugin.getDataFolder(), name);
		if (!file.exists()) {
			plugin.saveResource(name, true);
		}
		return load(plugin, file);
	}
	
	/**
	 * 以 UTF-8 的格式载入配置文件
	 * 
	 * @return
	 */
	public static FileConfiguration load(Plugin plugin, File file) {
		return loadYaml(plugin, file);
	}
	
	public static YamlConfiguration loadYaml(Plugin plugin, File file) {
		YamlConfiguration yaml = new YamlConfiguration();
		try {
			yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
		} catch (Exception e) {
			TLib.getTLib().getLogger().error("配置文件载入失败!");
			TLib.getTLib().getLogger().error("插件: &4" + plugin.getName());
			TLib.getTLib().getLogger().error("文件: &4" + file);
		}
		return yaml;
	}
	
	@Deprecated
	public static FileConfiguration load(Plugin plugin, String file) {
		return load(plugin, FileUtils.file(file));
	}
}
