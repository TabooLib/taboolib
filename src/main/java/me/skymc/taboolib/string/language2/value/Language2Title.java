package me.skymc.taboolib.string.language2.value;

import lombok.Getter;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.display.TitleUtils;
import me.skymc.taboolib.string.language2.Language2Format;
import me.skymc.taboolib.string.language2.Language2Line;
import me.skymc.taboolib.string.language2.Language2Value;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author sky
 * @since 2018年2月13日 下午3:58:07
 */
public class Language2Title implements Language2Line {
	
	private static final String KEY_TITLE = "    title: ";
	private static final String KEY_SUBTITLE = "    subtitle: ";
	private static final String KEY_STAYRULE = "    stay: ";
	
	@Getter
	private String title = "";
	
	@Getter
	private String subtitle = "";
	
	@Getter
	private int fade1 = 0;
	
	@Getter
	private int fade2 = 0;
	
	@Getter
	private int stay = 20;
	
	@Getter
	private Language2Value value;
	
	public Language2Title(Language2Format format, List<String> list) {
		// 变量初始化
		this.value = format.getLanguage2Value();
		// 遍历文本
		for (String message : list) {
			try {
				// 大标题
				if (message.startsWith(KEY_TITLE)) {
					title = message.substring(KEY_TITLE.length());
				}
				// 小标题
				else if (message.startsWith(KEY_SUBTITLE)) {
					subtitle = message.substring(KEY_SUBTITLE.length());
				}
				// 持续时间
				else if (message.startsWith(KEY_STAYRULE)) {
					String rule = message.substring(KEY_STAYRULE.length());
					fade1 = Integer.valueOf(rule.split("\\|")[0]);
					stay = Integer.valueOf(rule.split("\\|")[1]);
					fade2 = Integer.valueOf(rule.split("\\|")[2]);
				}
			}
			catch (Exception e) {
				// 识别异常
				title = ChatColor.DARK_RED + "[<ERROR-10: " + value.getLanguageKey() + ">]";
				subtitle = ChatColor.DARK_RED + "[<ERROR-10: " + value.getLanguageKey() + ">]";
			}
		}
	}
	
	@Override
	public void send(Player player) {
		// 检查版本
		if (TabooLib.getVerint() < 10800) {
			player.sendMessage(ChatColor.DARK_RED + "[<ERROR-31: " + value.getLanguageKey() + ">]");
		}
		else {
			TitleUtils.sendTitle(player, title, subtitle, fade1, stay, fade2);
		}
	}

	@Override
	public void console() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[<ERROR-40: " + value.getLanguageKey() + ">]");
	}
}
