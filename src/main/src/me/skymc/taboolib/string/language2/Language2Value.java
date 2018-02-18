package me.skymc.taboolib.string.language2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import lombok.Getter;
import me.skymc.taboolib.string.language2.type.Language2Action;
import me.skymc.taboolib.string.language2.type.Language2Json;
import me.skymc.taboolib.string.language2.type.Language2Title;

/**
 * @author sky
 * @since 2018年2月13日 下午3:05:15
 */
public class Language2Value {
	
	@Getter
	private Language2 language;
	
	@Getter
	private String languageKey;
	
	@Getter
	private List<String> languageValue;
	
	@Getter
	private Language2Type languageType;
	
	@Getter
	private LinkedHashMap<String, String> placeholder = new LinkedHashMap<>();
	
	@Getter
	private boolean enablePlaceholderAPI = false;
	
	/**
	 * 构造方法
	 */
	public Language2Value(Language2 language, String languageKey) {
		// 如果语言文件不存在
		if (language == null || languageKey == null) {
			languageValue = Arrays.asList(ChatColor.DARK_RED + "[<ERROR-0>]");
			return;
		}
		
		// 如果语言文本不存在
		if (!language.getConfiguration().contains(languageKey)) {
			languageValue = Arrays.asList(ChatColor.DARK_RED + "[<ERROR-1: " + languageKey + ">]");
			return;
		}
		
		// 如果不是集合类型
		if (language.getConfiguration().get(languageKey) instanceof List) {
			// 设置文本
			languageValue = asColored(language.getConfiguration().getStringList(languageKey));
			// 获取类型
			String type = languageValue.get(0).toLowerCase();
			
			// 是否有类型注释
			boolean isType = true;
			
			// 是否启用PAPI
			if (type.contains("[papi]")) {
				enablePlaceholderAPI = true;
			}
			
			// 判断类型
			if (type.contains("[json]")) {
				languageType = Language2Type.JSON;
			}
			else if (type.contains("[title]")) {
				languageType = Language2Type.TITLE;
			}
			else if (type.contains("[action]")) {
				languageType = Language2Type.ACTION;
			}
			else {
				languageType = Language2Type.TEXT;
				isType = false;
			}
			
			// 是否需要删除类型注释
			if (isType) {
				languageValue.remove(0);
			}
		}
		else {
			// 设置文本
			languageValue = Arrays.asList(ChatColor.translateAlternateColorCodes('&', language.getConfiguration().getString(languageKey)));
			// 设置类型
			languageType = Language2Type.TEXT;
		}
		
		// 初始化变量
		this.language = language;
		this.languageKey = languageKey;
	}
	
	/**
	 * 向玩家发送信息
	 * 
	 * @param player
	 */
	public void send(Player player) {
		// 标题类型
		if (languageType == Language2Type.TITLE) {
			// 发送文本
			new Language2Title(this).send(player);
		}
		// 动作栏类型
		else if (languageType == Language2Type.ACTION) {
			// 发送文本
			new Language2Action(this).send(player);
		}
		// JSON类型
		else if (languageType == Language2Type.JSON) {
			// 发送文本
			new Language2Json(this, player).send(player);
		}
		else {
			// 遍历文本
			for (String message : languageValue) {
				// 发送信息
				if (player != null) {
					player.sendMessage(setPlaceholder(message, player));
				}
				else {
					Bukkit.getConsoleSender().sendMessage(setPlaceholder(message, player));
				}
			}
		}
	}
	
	/**
	 * 向玩家发送信息
	 * 
	 * @param players 玩家
	 */
	public void send(List<Player> players) {
		// 标题类型
		if (languageType == Language2Type.TITLE) {
			// 识别文本
			Language2Title title = new Language2Title(this);
			// 发送文本
			players.forEach(x -> title.send(x));
		}
		// 动作栏类型
		else if (languageType == Language2Type.ACTION) {
			// 识别文本
			Language2Action action = new Language2Action(this);
			// 发送文本
			players.forEach(x -> action.send(x));
		}
		// JSON类型
		else if (languageType == Language2Type.JSON) {
			for (Player player : players) {
				// 识别文本
				Language2Json json = new Language2Json(this, player);
				// 发送文本
				json.send(player);
			}
		}
		else {
			for (Player player : players) {
				// 遍历文本
				for (String message : languageValue) {
					// 发送信息
					if (player != null) {
						player.sendMessage(setPlaceholder(message, player));
					}
					else {
						Bukkit.getConsoleSender().sendMessage(setPlaceholder(message, player));
					}
				}
			}
		}
	}
	
	/**
	 * 向指令发送者发送信息
	 * 
	 * @param sender
	 */
	public void send(CommandSender sender) {
		if (sender instanceof Player) {
			send((Player) sender);
		}
		else {
			send(Bukkit.getPlayerExact(""));
		}
	}
	
	/**
	 * 获取文本
	 * 
	 * @return
	 */
	public String asString() {
		// 标题类型
		if (languageType == Language2Type.TITLE) {
			return new Language2Title(this).getTitle();
		}
		// 动作栏类型
		else if (languageType == Language2Type.ACTION) {
			return new Language2Action(this).getText();
		}
		// JSON类型
		else if (languageType == Language2Type.JSON) {
			return new Language2Json(this, null).getText().toString();
		}
		else {
			return languageValue.size() == 0 ? ChatColor.DARK_RED + "[<ERROR-1>]" : languageValue.get(0);
		}
	}
	
	/**
	 * 变量替换
	 * 
	 * @param value 替换文本
	 * @param player 检测玩家
	 * @return String
	 */
	public String setPlaceholder(String value, Player player) {
		for (Entry<String, String> entry : placeholder.entrySet()) {
			value = value.replace(entry.getKey(), entry.getValue());
		}
		return isEnablePlaceholderAPI() ? this.language.setPlaceholderAPI(player, value) : value;
	}
	
	/**
	 * 变量替换构造
	 * 
	 * @param key 键
	 * @param value 值
	 * @return {@link Language2Value}
	 */
	public Language2Value addPlaceholder(String key, String value) {
		this.placeholder.put(key, value);
		return this;
	}
	
	/**
	 * 替换颜色
	 * 
	 * @param list
	 * @return
	 */
	public List<String> asColored(List<String> list) {
		for (int i = 0 ; i < list.size() ; i++) {
			list.set(i, ChatColor.translateAlternateColorCodes('&', list.get(i)));
		}
		return list;
	}
}
