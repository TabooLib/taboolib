package com.ilummc.tlib.resources.type;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ilummc.tlib.compat.PlaceholderHook;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.resources.TLocaleSerialize;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.SerializableAs;

import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.stream.Collectors;

@Immutable
@SerializableAs("TEXT")
@SuppressWarnings({"unchecked", "rawtypes"})
public class TLocaleText extends TLocaleSerialize {

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

    public static TLocaleText of(String s) {
        return new TLocaleText(TLocale.Translate.setColored(s), TLocale.Translate.isPlaceholderUseDefault());
    }

    public static TLocaleText of(Object o) {
        return o instanceof String ? of(((String) o)) : new TLocaleText(o, false);
    }

    public static TLocaleText valueOf(Map<String, Object> map) {
        if (map.containsKey("text")) {
            Object object = map.get("text");
            if (object instanceof String[]) {
                return new TLocaleText(Arrays.stream(((String[]) object)).collect(Collectors.toList()), isPlaceholderEnabled(map));
            } else {
                return new TLocaleText(Objects.toString(object), isPlaceholderEnabled(map));
            }
        }
        return new TLocaleText("§cError chat message loaded.", TLocale.Translate.isPlaceholderUseDefault());
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (text instanceof String) {
            sender.sendMessage(replaceText(sender, Strings.replaceWithOrder((String) text, args)));
        } else if (text instanceof List) {
            ((List) text).forEach(s -> sender.sendMessage(replaceText(sender, Strings.replaceWithOrder(String.valueOf(s), args))));
        }
    }

    @Override
    public String asString(String... args) {
        return Strings.replaceWithOrder(TLocale.Translate.setColored(objectToString(text)), args);
    }

    @Override
    public List<String> asStringList(String... args) {
        if (text instanceof List) {
            return ((List<String>) text).stream().map(x -> Strings.replaceWithOrder(TLocale.Translate.setColored(x), args)).collect(Collectors.toList());
        } else {
            return Collections.singletonList(asString(args));
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
        return usePlaceholder ? Maps.newHashMap(ImmutableMap.of("text", text, "papi", true)) : Maps.newHashMap(ImmutableMap.of("text", text));
    }

    private String replaceText(CommandSender sender, String args) {
        return usePlaceholder ? TLocale.Translate.setPlaceholders(sender, args) : TLocale.Translate.setColored(args);
    }

    private String objectToString(Object text) {
        if (text instanceof String) {
            return ((String) text);
        } else {
            StringJoiner joiner = new StringJoiner("\n");
            ((List<String>) text).forEach(joiner::add);
            return joiner.toString();
        }
    }
}
