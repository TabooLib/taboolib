package com.ilummc.tlib.resources.type;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.concurrent.Immutable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.compat.PlaceholderHook;
import com.ilummc.tlib.resources.TLocaleSendable;
import com.ilummc.tlib.util.Strings;

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
        }
        else if (text instanceof List) {
            this.text = ImmutableList.copyOf(((List) text));
        }
        else {
            throw new IllegalArgumentException("Param 'text' can only be an instance of String or String[] or List<String>");
        }
    }
    
    private String replaceMsg(CommandSender sender, String s) {
        return usePlaceholder ? PlaceholderHook.replace(sender, s) : s;
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (text instanceof String) {
            sender.sendMessage(replaceMsg(sender, Strings.replaceWithOrder((String) text, args)));
        }
        else if (text instanceof List) {
            ((List) text).forEach(s -> sender.sendMessage(replaceMsg(sender, Strings.replaceWithOrder(String.valueOf(s), args))));
        }
    }
    
    @Override
    public String asString(String... args) {
    	return Strings.replaceWithOrder((String) text, args);
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

    public static TLocaleText valueOf(Map<String, Object> map) {
        if (map.containsKey("text")) {
            Object object = map.get("text");
            Object objPapi = map.getOrDefault("papi", TLib.getTLib().getConfig().isEnablePlaceholderHookByDefault());
            boolean papi = objPapi instanceof Boolean ? (boolean) objPapi : objPapi instanceof String && objPapi.equals("true");
            if (object instanceof List) {
                return new TLocaleText(((List<String>) object).stream()
                        .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                        .collect(Collectors.toList()), papi);
            }
            else if (object instanceof String[]) {
                return new TLocaleText(Arrays.stream(((String[]) object))
                        .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                        .collect(Collectors.toList()), papi);
            }
            else {
                return new TLocaleText(ChatColor.translateAlternateColorCodes('&', Objects.toString(object)), papi);
            }
        }
        return new TLocaleText("§cError chat message loaded.", TLib.getTLib().getConfig().isEnablePlaceholderHookByDefault());
    }

    public static TLocaleText of(String s) {
        return new TLocaleText(ChatColor.translateAlternateColorCodes('&', s), TLib.getTLib().getConfig().isEnablePlaceholderHookByDefault());
    }
}
