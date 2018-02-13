package me.skymc.taboolib.string.language2.type;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import lombok.Getter;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.ClickEvent;
import me.skymc.taboolib.jsonformatter.click.RunCommandEvent;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.HoverEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;
import me.skymc.taboolib.string.language2.Language2Value;

/**
 * @author sky
 * @since 2018年2月13日 下午4:11:33
 */
public class Language2Json {
	
	private static final String KEY_TEXT = "    text: ";
	private static final String KEY_COMMAND = "    command: ";
	private static final String KEY_SUGGEST = "    suggest: ";
	
	@Getter
	private Player player;
	
	@Getter
	private Language2Value value;
	
	@Getter
	private JSONFormatter json = new JSONFormatter();
	
	@Getter
	private StringBuffer text = new StringBuffer();
	
	public Language2Json(Language2Value value, Player player) {
		// 文本初始化
		String current = ChatColor.DARK_RED + "[<ERROR-20: " + value.getLanguageKey() + ">]";
		
		// 首次检测
		boolean isFirst = true;
		boolean isBreak = false;
		
		// 变量初始化
		this.value = value;
		this.player = player;
		
		// 动作初始化
		ClickEvent clickEvent = null;
		HoverEvent hoverEvent = null;
		
		// 遍历文本
		for (String message : value.getLanguageValue()) {
			try {
				// 如果是显示文本
				if (message.startsWith(KEY_TEXT)) {
					hoverEvent = new ShowTextEvent(message.replace("||", "\n").substring(KEY_TEXT.length()));
				}
				// 执行指令
				else if (message.startsWith(KEY_COMMAND)) {
					clickEvent = new RunCommandEvent(message.substring(KEY_COMMAND.length()));
				}
				// 打印指令
				else if (message.startsWith(KEY_SUGGEST)) {
					clickEvent = new SuggestCommandEvent(message.substring(KEY_SUGGEST.length()));
				}
				// 换行
				else if (message.equals("[break]")) {
					append(current, clickEvent, hoverEvent);
					// 删除动作
					clickEvent = null;
					hoverEvent = null;
					// 换行
					json.newLine();
					// 标记
					isBreak = true;
				}
				// 新内容
				else {
					if (!isFirst && !isBreak) {
						append(current, clickEvent, hoverEvent);
						// 删除动作
						clickEvent = null;
						hoverEvent = null;
					}
					// 更新
					current = message;
					// 标记
					isFirst = false;
					isBreak = false;
				}
			}
			catch (Exception e) {
				// 识别异常
				json.append(ChatColor.DARK_RED + "[<ERROR-21: " + value.getLanguageKey() + ">]");
			}
		}
		// 追加
		append(current, clickEvent, hoverEvent);
	}
	
	/**
	 * 发送给玩家
	 * 
	 * @param player 玩家
	 */
	public void send(Player player) {
		if (player != null) {
			json.send(player);
		}
		else {
			Bukkit.getConsoleSender().sendMessage(text.toString());
		}
	}
	
	/**
	 * 追加 JSON 内容
	 * 
	 * @param current 文本
	 * @param clickevent 点击动作
	 * @param hoverEvent 显示动作
	 */
	private void append(String current, ClickEvent clickEvent, HoverEvent hoverEvent) {
		if (clickEvent == null && hoverEvent == null) {
			// 纯文本
			json.append(value.setPlaceholder(current, player));
		} else if (clickEvent != null && hoverEvent == null) {
			// 纯点击
			json.appendClick(value.setPlaceholder(current, player), clickEvent);
		} else if (clickEvent == null && hoverEvent != null) {
			// 纯显示
			json.appendHover(value.setPlaceholder(current, player), hoverEvent);
		} else {
			// 全部
			json.appendHoverClick(value.setPlaceholder(current, player), hoverEvent, clickEvent);
		}
		// 追加显示文本
		text.append(current);
	}
}
