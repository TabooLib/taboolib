package me.skymc.taboolib.string.language2.value;

import me.skymc.taboolib.string.language2.Language2Format;
import me.skymc.taboolib.string.language2.Language2Line;
import me.skymc.taboolib.string.language2.Language2Value;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sky
 * @since 2018-03-08 22:43:27
 */
public class Language2Text implements Language2Line {

    private List<String> text = new ArrayList<>();

    private Language2Value value;

    public Language2Text(Language2Format format, List<String> list) {
        this.value = format.getLanguage2Value();
        // 遍历文本
        text.addAll(list);
    }

    public List<String> getText() {
        return text;
    }

    public Language2Value getValue() {
        return value;
    }

    @Override
    public void send(Player player) {
        for (String line : text) {
            player.sendMessage(line);
        }
    }

    @Override
    public void console() {
        for (String line : text) {
            Bukkit.getConsoleSender().sendMessage(line);
        }
    }
}
