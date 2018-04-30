package com.ilummc.tlib.resources.type;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.compat.PlaceholderHook;
import com.ilummc.tlib.resources.TLocaleSendable;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ThreadSafe
@SerializableAs("JSON")
public class TLocaleJson implements TLocaleSendable, ConfigurationSerializable {

    private static final Pattern pattern = Pattern.compile("<([^<>]*)?@([^<>]*)>");
    private final List<BaseComponent[]> components;
    private final boolean papi;
    private final Map<String, Object> map;

    private TLocaleJson(List<BaseComponent[]> components, boolean papi, Map<String, Object> map) {
        this.components = ImmutableList.copyOf(components);
        this.papi = papi;
        this.map = map;
    }

    public static TLocaleJson valueOf(Map<String, Object> map) {
        Object textObj = map.getOrDefault("text", "Empty Node");
        List<String> textList = textObj instanceof String ? Lists.newArrayList(((String) textObj)) :
                (textObj instanceof List && !((List) textObj).isEmpty()) ?
                        ((List<?>) textObj).stream().map(Object::toString).collect(Collectors.toList()) : Lists.newArrayList(String.valueOf(textObj));
        boolean papi = (boolean) map.getOrDefault("papi", Main.getInst().getConfig().getBoolean("LOCALE.USE_PAPI", false));
        Object argsObj = map.get("args");
        if (argsObj instanceof Map) {
            Map<?, ?> section = ((Map<?, ?>) argsObj);
            List<BaseComponent[]> collect = textList.stream().map(s -> {
                String[] template = pattern.split(s);
                int index = 0;
                Matcher matcher = pattern.matcher(s);
                List<BaseComponent> builder;
                if (template.length > index) {
                    builder = new ArrayList<>(Arrays.asList(TextComponent.fromLegacyText(template[index++])));
                } else builder = new ArrayList<>();
                while (matcher.find()) {
                    String replace = matcher.group();
                    if (replace.length() <= 2) continue;
                    replace = replace.substring(1, replace.length() - 1);
                    String[] split = replace.split("@");
                    String text = split.length > 1 ? split[0] : "";
                    String node = split.length > 1 ? split[1] : split[0];
                    if (section.containsKey(node)) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> arg = (Map<String, Object>) section.get(node);
                        text = (String) arg.getOrDefault("text", text);
                        BaseComponent[] component = TextComponent.fromLegacyText(text);
                        arg.forEach((key, value) -> {
                            switch (key) {
                                case "suggest":
                                    for (BaseComponent baseComponent : component) {
                                        baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.valueOf(value)));
                                    }
                                    break;
                                case "command":
                                    for (BaseComponent baseComponent : component) {
                                        baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf(value)));
                                    }
                                    break;
                                case "hover":
                                    for (BaseComponent baseComponent : component) {
                                        baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(value)).create()));
                                    }
                                    break;
                                default:
                            }
                        });
                        builder.addAll(Arrays.asList(component));
                    } else {
                        builder.addAll(Arrays.asList(TextComponent.fromLegacyText(text)));
                        TLib.getTLib().getLogger().warn(Strings.replaceWithOrder(TLib.getTLib().getInternalLang().getString("MISSING-ARGUMENT"), node));
                    }
                    if (index < template.length) {
                        builder.addAll(Arrays.asList(TextComponent.fromLegacyText(template[index++])));
                    }
                }
                return builder.toArray(new BaseComponent[0]);
            }).collect(Collectors.toList());
            return new TLocaleJson(collect, papi, map);
        }
        return new TLocaleJson(textList.stream().map(TextComponent::fromLegacyText).collect(Collectors.toList()), papi, map);
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (sender instanceof Player)
            components.forEach(comp -> ((Player) sender).spigot().sendMessage(replace(comp, sender, args)));
    }

    @Override
    public String asString(String... args) {
        return components.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        return Maps.newHashMap(map);
    }

    private BaseComponent[] replace(BaseComponent[] component, CommandSender sender, String... args) {
        BaseComponent[] components = new BaseComponent[component.length];
        for (int i = 0; i < components.length; i++) {
            components[i] = replace(component[i].duplicate(), sender, args);
        }
        return components;
    }

    private List<BaseComponent> replace(List<BaseComponent> component, CommandSender sender, String... args) {
        return component.stream().map(c -> replace(c, sender, args)).collect(Collectors.toList());
    }

    private BaseComponent replace(BaseComponent component, CommandSender sender, String... args) {
        if (component.getClickEvent() != null) {
            ClickEvent clickEvent = new ClickEvent(component.getClickEvent().getAction(), replace(sender, component.getClickEvent().getValue(), args));
            component.setClickEvent(clickEvent);
        }
        if (component.getHoverEvent() != null) {
            HoverEvent hoverEvent = new HoverEvent(component.getHoverEvent().getAction(), replace(component.getHoverEvent().getValue(), sender, args));
            component.setHoverEvent(hoverEvent);
        }
        if (component.getExtra() != null)
            component.setExtra(replace(component.getExtra(), sender, args));
        if (component instanceof TextComponent) {
            ((TextComponent) component).setText(replace(sender, ((TextComponent) component).getText(), args));
        }
        return component;
    }

    private String replace(CommandSender sender, String text, String[] args) {
        String s = Strings.replaceWithOrder(text, args);
        return papi ? PlaceholderHook.replace(sender, s) : s;
    }
}
