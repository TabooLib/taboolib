package com.ilummc.tlib.resources.type;

import com.google.common.collect.Maps;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.resources.TLocaleSerialize;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.display.TitleUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import javax.annotation.concurrent.Immutable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bkm016
 * @since 2018-04-22
 */

@Immutable
@SerializableAs("TITLE")
public class TLocaleTitle extends TLocaleSerialize {

    private final String title;
    private final String subtitle;
    private final int fadein;
    private final int fadeout;
    private final int stay;

    private boolean usePlaceholder;

    private TLocaleTitle(String title, String subString, int fadein, int fadeout, int stay, boolean usePlaceholder) {
        this.title = title;
        this.subtitle = subString;
        this.fadein = fadein;
        this.fadeout = fadeout;
        this.stay = stay;
        this.usePlaceholder = usePlaceholder;
    }

    public static TLocaleTitle valueOf(Map<String, Object> map) {
        TLocaleTitle title;
        try {
            title = new TLocaleTitle(
                    getStringOrDefault(map, "title", ""),
                    getStringOrDefault(map, "subtitle", ""),
                    getIntegerOrDefault(map, "fadein", 10),
                    getIntegerOrDefault(map, "fadeout", 10),
                    getIntegerOrDefault(map, "stay", 10),
                    isPlaceholderEnabled(map));
        } catch (Exception e) {
            title = new TLocaleTitle("Empty Title message.", e.getMessage(), 10, 20, 10, false);
        }
        return title;
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (sender instanceof Player) {
            TitleUtils.sendTitle((Player) sender, replaceText(sender, title), replaceText(sender, subtitle), fadein, stay, fadeout);
        } else {
            TLocale.Logger.error("LOCALE.TITLE-SEND-TO-NON-PLAYER", asString(args));
        }
    }

    @Override
    public String asString(String... args) {
        return Strings.replaceWithOrder(Strings.replaceWithOrder("TITLE: [title: ''{0}'', subtitle: ''{1}'', fadeIn: {2}, fadeOut: {3}]", title, subtitle, fadein, fadeout), args);
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("papi", usePlaceholder);
        map.put("title", title);
        map.put("subtitle", subtitle);
        map.put("fadein", fadein);
        map.put("fadeout", fadeout);
        map.put("stay", stay);
        return map;
    }

    private String replaceText(CommandSender sender, String args) {
        return usePlaceholder ? TLocale.Translate.setPlaceholders(sender, args) : TLocale.Translate.setColored(args);
    }
}
