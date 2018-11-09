package com.ilummc.tlib.resources.type;

import com.google.common.collect.Maps;
import com.ilummc.tlib.compat.PlaceholderHook;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.resources.TLocaleSerialize;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.display.ActionUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import javax.annotation.concurrent.Immutable;
import java.util.Map;

@Immutable
@SerializableAs("ACTION")
public class TLocaleActionBar extends TLocaleSerialize {

    private final String text;
    private final boolean papi;

    private TLocaleActionBar(String text, boolean papi) {
        this.text = text;
        this.papi = papi;
    }

    public static TLocaleActionBar valueOf(Map<String, Object> map) {
        String text = ChatColor.translateAlternateColorCodes('&', String.valueOf(map.getOrDefault("text", "Empty Action bar message.")));
        return new TLocaleActionBar(text, isPlaceholderEnabled(map));
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (sender instanceof Player) {
            TLocale.Display.sendActionBar(((Player) sender), replace(sender, text, args));
        }
    }

    private String replace(CommandSender sender, String text, String[] args) {
        String s = Strings.replaceWithOrder(text, args);
        return papi ? PlaceholderHook.replace(sender, s) : s;
    }

    @Override
    public String asString(String... args) {
        return "ActionBar: [" + text + "]";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("text", text);
        if (papi) {
            map.put("papi", true);
        }
        return map;
    }

}
