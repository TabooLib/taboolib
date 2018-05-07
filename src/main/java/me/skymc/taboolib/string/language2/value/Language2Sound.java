package me.skymc.taboolib.string.language2.value;

import me.skymc.taboolib.sound.SoundPack;
import me.skymc.taboolib.string.language2.Language2Format;
import me.skymc.taboolib.string.language2.Language2Line;
import me.skymc.taboolib.string.language2.Language2Value;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sky
 * @since 2018-03-08 22:43:27
 */
public class Language2Sound implements Language2Line {

    private List<SoundPack> sounds = new ArrayList<>();

    private Language2Value value;

    public List<SoundPack> getSounds() {
        return sounds;
    }

    public Language2Value getValue() {
        return value;
    }

    public Language2Sound(Language2Format format, List<String> list) {
        this.value = format.getLanguage2Value();
        // 遍历文本
        for (String line : list) {
            sounds.add(new SoundPack(line));
        }
    }

    @Override
    public void send(Player player) {
        for (SoundPack sound : sounds) {
            sound.play(player);
        }
    }

    @Override
    public void console() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[<ERROR-40: " + value.getLanguageKey() + ">]");
    }
}
