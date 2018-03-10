package me.skymc.taboolib.string.language2;

import java.io.File;
import java.io.FileNotFoundException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.skymc.taboolib.fileutils.ConfigUtils;

/**
 * @author sky
 * @since 2018年2月13日 下午2:37:07
 */
public class Language2 {
	
	@Getter
	private FileConfiguration configuration;
	
	@Getter
	private File languageFile;
	
	@Getter
	private File languageFolder;
	
	@Getter
	private Plugin plugin;
	
	@Getter
	private String languageName;
	
	/**
	 * 构造方法
	 * 
	 * @param plugin 插件
	 */
	public Language2(Plugin plugin) {
		this("zh_CN", plugin);
	}
	
	/**
	 * 构造方法
	 * 
	 * @param languageName 语言文件
	 * @param plugin 插件
	 */
	public Language2(String languageName, Plugin plugin) {
		this.languageName = languageName;
		this.plugin = plugin;
		// 重载语言文件
		reload(languageName);
	}
	
	/**
	 * 获取语言文件
	 * 
	 * @param key 键
	 * @return {@link Language2Value}
	 */
	public Language2Value get(String key) {
		return new Language2Value(this, key);
	}
	
	/**
	 * 获取语言文件
	 * 
	 * @param key 键
	 * @param placeholder 替换变量，从 @$0 开始
	 * @return {@link Language2Value}
	 */
	public Language2Value get(String key, String... placeholder) {
		Language2Value value = new Language2Value(this, key);
		for (int i = 0 ; i < placeholder.length ; i++) {
			value.addPlaceholder("$" + i, placeholder[i]);
		}
		return value;
	}
	
	/**
	 * 重载语言文件
	 */
	public void reload() {
		reload(this.languageName);
	}
	
	/**
	 * 重载语言文件
	 * 
	 * @param languageName 新语言文件名称
	 */
	public void reload(String languageName) {
		// 初始化文件夹
		createFolder(plugin);
		// 格式化配置名
		languageName = formatName(languageName);
		// 获取文件
		languageFile = new File(languageFolder, languageName);
		// 文件不存在
		if (!languageFile.exists()) {
			// 如果语言文件不存在
			if (plugin.getResource("Language2/" + languageName) == null) {
				try {
					throw new FileNotFoundException("语言文件 " + languageName + " 不存在");
				}
				catch (Exception e) {
					// TODO: handle exception
				}
			}
			else {
				// 释放资源
				plugin.saveResource("Language2/" + languageName, true);
			}
		}
		// 载入配置
		configuration = ConfigUtils.load(plugin, languageFile);
	}
	
	/**
	 * PlaceholderAPI 变量识别
	 * 
	 * @param player 玩家
	 * @param string 文本
	 * @return String
	 */
	public String setPlaceholderAPI(Player player, String string) {
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && player != null) {
			return PlaceholderAPI.setPlaceholders(player, string);
		}
		return string;
	}
	
	/**
	 * 语言文件名称格式化
	 * 
	 * @param name 语言文件名称
	 * @return String
	 */
	private String formatName(String name) {
		return name.contains(".yml") ? name : name + ".yml";
	}
	
	/**
	 * 语言文件夹初始化
	 * 
	 * @param plugin
	 */
	private void createFolder(Plugin plugin) {
		languageFolder = new File(plugin.getDataFolder(), "Language2");
		if (!languageFolder.exists()) {
			languageFolder.mkdir();
		}
	}
}
