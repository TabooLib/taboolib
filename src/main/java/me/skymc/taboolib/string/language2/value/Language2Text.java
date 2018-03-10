package me.skymc.taboolib.string.language2.value;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import lombok.Getter;
import me.skymc.taboolib.sound.SoundPack;
import me.skymc.taboolib.string.language2.Language2Format;
import me.skymc.taboolib.string.language2.Language2Line;
import me.skymc.taboolib.string.language2.Language2Value;

/**
 * @author sky
 * @since 2018-03-08 22:43:27
 */
public class Language2Text implements Language2Line {
	
	@Getter
	private List<String> text = new ArrayList<>(); 
	
	@Getter
	private Language2Value value;
	
	public Language2Text(Language2Format format, List<String> list) {
		this.value = format.getLanguage2Value();
		// 遍历文本
		for (String line : list) {
			text.add(line);
		}
	}

	@Override
	public void send(Player player) {
		for (String line : text) {
			player.sendMessage(value.setPlaceholder(line, player));
		}
	}

	@Override
	public void console() {
		for (String line : text) {
			Bukkit.getConsoleSender().sendMessage(value.setPlaceholder(line, null));
		}
	}
}
