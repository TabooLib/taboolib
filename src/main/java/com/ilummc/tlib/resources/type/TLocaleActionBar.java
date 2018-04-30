package com.ilummc.tlib.resources.type;

import com.google.common.collect.Maps;
import com.ilummc.tlib.compat.PlaceholderHook;
import com.ilummc.tlib.nms.ActionBar;
import com.ilummc.tlib.resources.TLocaleSendable;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import javax.annotation.concurrent.Immutable;
import java.util.Map;

@Immutable
@SerializableAs("ACTION")
public class TLocaleActionBar implements TLocaleSendable, ConfigurationSerializable {

    private final String text;

    private final boolean papi;

    private TLocaleActionBar(String text, boolean papi) {
        this.text = text;
        this.papi = papi;
    }

    public static TLocaleActionBar valueOf(Map<String, Object> map) {
        String text = String.valueOf(map.getOrDefault("text", "Empty Action bar message."));
        boolean papi = (boolean) map.getOrDefault("papi", Main.getInst().getConfig().getBoolean("LOCALE.USE_PAPI", false));
        return new TLocaleActionBar(text, papi);
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (sender instanceof Player)
            ActionBar.sendActionBar(((Player) sender), replace(sender, text, args));
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
        if (papi)
            map.put("papi", true);
        return map;
    }

}
