package com.ilummc.tlib.resources.type;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ilummc.tlib.compat.PlaceholderHook;
import com.ilummc.tlib.resources.TLocaleSendable;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.stream.Collectors;

@Immutable
@SerializableAs("TEXT")
@SuppressWarnings({"unchecked", "rawtypes"})
public class TLocaleText implements TLocaleSendable, ConfigurationSerializable {

    private final Object text;

    private final boolean usePlaceholder;

    private TLocaleText(Object text, boolean usePlaceholder) {
        this.usePlaceholder = usePlaceholder;
        if (text instanceof String) {
            this.text = text;
        } else if (text instanceof List) {
            this.text = ImmutableList.copyOf(((List) text));
        } else {
            throw new IllegalArgumentException("Param 'text' can only be an instance of String or List<String>");
        }
    }

    private String replaceMsg(CommandSender sender, String s) {
        return usePlaceholder ? PlaceholderHook.replace(sender, s) : s;
    }

    public static TLocaleText valueOf(Map<String, Object> map) {
        if (map.containsKey("text")) {
            Object object = map.get("text");
            Object objPapi = map.getOrDefault("papi", Main.getInst().getConfig().getBoolean("LOCALE.USE_PAPI", false));
            boolean papi = objPapi instanceof Boolean ? (boolean) objPapi : objPapi instanceof String && objPapi.equals("true");
            if (object instanceof List) {
                return new TLocaleText(((List<String>) object).stream()
                        .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                        .collect(Collectors.toList()), papi);
            } else if (object instanceof String[]) {
                return new TLocaleText(Arrays.stream(((String[]) object))
                        .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                        .collect(Collectors.toList()), papi);
            } else {
                return new TLocaleText(ChatColor.translateAlternateColorCodes('&', Objects.toString(object)), papi);
            }
        }
        return new TLocaleText("§cError chat message loaded.", Main.getInst().getConfig().getBoolean("LOCALE.USE_PAPI", false));
    }

    public static TLocaleText of(String s) {
        return new TLocaleText(ChatColor.translateAlternateColorCodes('&', s), Main.getInst().getConfig().getBoolean("LOCALE.USE_PAPI", false));
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (text instanceof String) {
            sender.sendMessage(replaceMsg(sender, Strings.replaceWithOrder((String) text, args)));
        } else if (text instanceof List) {
            ((List) text).forEach(s -> sender.sendMessage(replaceMsg(sender, Strings.replaceWithOrder(String.valueOf(s), args))));
        }
    }

    @Override
    public String asString(String... args) {
        return Strings.replaceWithOrder(objectToString(text), args);
    }

    private String objectToString(Object text) {
        if (text instanceof String) return ((String) text);
        else {
            StringJoiner joiner = new StringJoiner("\n");
            ((List<String>) text).forEach(joiner::add);
            return joiner.toString();
        }
    }

    @Override
    public String toString() {
        if (text instanceof String[]) {
            return Arrays.toString((String[]) text);
        } else {
            return text.toString();
        }
    }

    @Override
    public Map<String, Object> serialize() {
        if (usePlaceholder) {
            return Maps.newHashMap(ImmutableMap.of("text", text, "papi", true));
        }
        return Maps.newHashMap(ImmutableMap.of("text", text));
    }
}
