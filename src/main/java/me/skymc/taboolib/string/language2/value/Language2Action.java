package me.skymc.taboolib.string.language2.value;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.display.ActionUtils;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.string.language2.Language2Format;
import me.skymc.taboolib.string.language2.Language2Line;
import me.skymc.taboolib.string.language2.Language2Value;

/**
 * @author sky
 * @since 2018年2月13日 下午3:58:07
 */
public class Language2Action implements Language2Line {
	
	private static final String KEY_TEXT = "    text: ";
	private static final String KEY_STAY = "    repeat: ";
	
	@Getter
	private String text = "";
	
	@Getter
	private int repeat = 1;
	
	@Getter
	private Language2Value value;
	
	public Language2Action(Language2Format format, List<String> list) {
		// 变量初始化
		this.value = format.getLanguage2Value();
		// 遍历文本
		for (String message : list) {
			try {
				// 动作栏提示
				if (message.startsWith(KEY_TEXT)) {
					text = message.substring(KEY_TEXT.length());
				}
				// 持续时间
				if (message.startsWith(KEY_STAY)) {
					repeat = NumberUtils.getInteger(message.substring(KEY_STAY.length()));
				}
			}
			catch (Exception e) {
				// 识别异常
				text = ChatColor.DARK_RED + "[<ERROR-11: " + value.getLanguageKey() + ">]";
			}
		}
		
		// 检查重复次数
		if (repeat < 0) {
			repeat = 1;
			text = ChatColor.DARK_RED + "[<ERROR-12: " + value.getLanguageKey() + ">]";
		}
	}
	
	/**
	 * 发送给玩家
	 * 
	 * @param player 玩家
	 */
	public void send(Player player) {
		// 检查版本
		if (TabooLib.getVerint() < 10800) {
			player.sendMessage(ChatColor.DARK_RED + "[<ERROR-30: " + value.getLanguageKey() + ">]");
		}
		else {
			new BukkitRunnable() {
				int times = 0;
				
				@Override
				public void run() {
					ActionUtils.send(player, value.setPlaceholder(text, player));
					if ((times += 1) >= repeat) {
						cancel();
					}
				}
			}.runTaskTimer(Main.getInst(), 0, 20);
		}
	}

	@Override
	public void console() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[<ERROR-40: " + value.getLanguageKey() + ">]");
	}
}
