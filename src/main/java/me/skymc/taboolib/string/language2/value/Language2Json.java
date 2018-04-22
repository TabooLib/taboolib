package me.skymc.taboolib.string.language2.value;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.ClickEvent;
import me.skymc.taboolib.jsonformatter.click.OpenUrlEvent;
import me.skymc.taboolib.jsonformatter.click.RunCommandEvent;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.HoverEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowItemEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;
import me.skymc.taboolib.string.language2.Language2Format;
import me.skymc.taboolib.string.language2.Language2Line;
import me.skymc.taboolib.string.language2.Language2Value;

/**
 * @author sky
 * @since 2018年2月13日 下午4:11:33
 */
public class Language2Json implements Language2Line {
	
	private static final String KEY_TEXT = "    text: ";
	private static final String KEY_COMMAND = "    command: ";
	private static final String KEY_SUGGEST = "    suggest: ";
	private static final String KEY_URL = "    url: ";
	private static final String KEY_ITEM = "    item: ";
	
	@Getter
	private Player player;
	
	@Getter
	private Language2Value value;
	
	@Getter
	private JSONFormatter json = new JSONFormatter();
	
	@Getter
	private StringBuffer text = new StringBuffer();
	
	public Language2Json(Language2Format format, List<String> list, Player player) {
		// 首次检测
		boolean isFirst = true;
		boolean isBreak = false;
		
		// 变量初始化
		this.value = format.getLanguage2Value();
		this.player = player;
		
		// 动作初始化
		ClickEvent clickEvent = null;
		HoverEvent hoverEvent = null;
		
		// 文本初始化
		String current = ChatColor.DARK_RED + "[<ERROR-20: " + value.getLanguageKey() + ">]";
		
		// 遍历文本
		for (String message : list) {
			try {
				// 如果是显示文本
				if (message.startsWith(KEY_TEXT)) {
					hoverEvent = new ShowTextEvent(message.replace("||", "\n").substring(KEY_TEXT.length()));
				}
				// 显示物品
				else if (message.startsWith(KEY_ITEM)) {
					ItemStack item = ItemUtils.getCacheItem(message.substring(KEY_ITEM.length()));
					if (item == null) {
						item = new ItemStack(Material.STONE);
					}
					hoverEvent = new ShowItemEvent(item);
				}
				// 执行指令
				else if (message.startsWith(KEY_COMMAND)) {
					clickEvent = new RunCommandEvent(message.substring(KEY_COMMAND.length()));
				}
				// 打印指令
				else if (message.startsWith(KEY_SUGGEST)) {
					clickEvent = new SuggestCommandEvent(message.substring(KEY_SUGGEST.length()));
				}
				// 打开连接
				else if (message.startsWith(KEY_URL)) {
					clickEvent = new OpenUrlEvent(message.substring(KEY_URL.length()));
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
		json.send(player);
	}
	
	@Override
	public void console() {
		Bukkit.getConsoleSender().sendMessage(text.toString());
	}

	/**
	 * 追加 JSON 内容
	 * 
	 * @param current 文本
	 * @param hoverEvent 显示动作
	 */
	private void append(String current, ClickEvent clickEvent, HoverEvent hoverEvent) {
		if (clickEvent == null && hoverEvent == null) {
			// 纯文本
			json.append(current);
		} else if (clickEvent != null && hoverEvent == null) {
			// 纯点击
			json.appendClick(current, clickEvent);
		} else if (clickEvent == null && hoverEvent != null) {
			// 纯显示
			json.appendHover(current, hoverEvent);
		} else {
			// 全部
			json.appendHoverClick(current, hoverEvent, clickEvent);
		}
		// 追加显示文本
		text.append(current);
	}
}
