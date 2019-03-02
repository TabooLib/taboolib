package com.ilummc.tlib.resources.type;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.bungee.api.chat.*;
import com.ilummc.tlib.bungee.chat.ComponentSerializer;
import com.ilummc.tlib.compat.PlaceholderHook;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.resources.TLocaleSerialize;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.json.tellraw.TellrawJson;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.string.VariableFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.SerializableAs;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@ThreadSafe
@SerializableAs("JSON")
public class TLocaleJson extends TLocaleSerialize {

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
        List<String> textList = getTextList(map.getOrDefault("text", "Empty Node"));

        // 分析 args 并替换
        Object argsObj = map.get("args");
        if (argsObj instanceof Map) {
            Map<String, Object> section = new HashMap<>(((Map<?, ?>) argsObj).size());

            // valueOf(k) 是因为这个键可能加载为一个 Integer 导致 contains(String) 返回 false
            ((Map<?, ?>) argsObj).forEach((k, v) -> section.put(String.valueOf(k), v));
            List<BaseComponent[]> collect = textList.stream().map(s -> {
                int index = 0;
                String[] template = pattern.split(s);
                Matcher matcher = pattern.matcher(s);
                // 有可能开头和结尾是替换文本，所以做个特判
                List<BaseComponent> builder = template.length > index ? new ArrayList<>(Arrays.asList(TextComponent.fromLegacyText(template[index++]))) : new ArrayList<>();
                while (matcher.find()) {
                    String replace = matcher.group();
                    // 假的 <@>
                    if (replace.length() <= 2) {
                        continue;
                    }
                    // 真的 <@xxx>
                    replace = replace.substring(1, replace.length() - 1);
                    String[] split = replace.split("@");
                    // @ 前面的字符串
                    String text = split.length > 1 ? split[0] : "";
                    // @ 后面的节点名
                    String node = split.length > 1 ? split[1] : split[0];
                    // 如果 args 有这个 xxx
                    if (section.containsKey(node)) {
                        Map<String, Object> arg = (Map<String, Object>) section.get(node);
                        text = TLocale.Translate.setColored(String.valueOf(arg.getOrDefault("text", text)));
                        // 可能有很多个 BaseComponent，于是为每个 component 单独设置各种事件
                        BaseComponent[] component = TextComponent.fromLegacyText(text);
                        arg.forEach((key, value) -> {
                            if (key.equalsIgnoreCase("suggest")) {
                                Arrays.stream(component).forEach(baseComponent -> baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.valueOf(value))));
                            } else if (key.equalsIgnoreCase("command") || "commands".equalsIgnoreCase(key)) {
                                Arrays.stream(component).forEach(baseComponent -> baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf(value))));
                            } else if (key.equalsIgnoreCase("hover")) {
                                Arrays.stream(component).forEach(baseComponent -> baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(TLocale.Translate.setColored(String.valueOf(value))).create())));
                            }
                        });
                        // 添加到原来的 list 里面
                        builder.addAll(Arrays.asList(component));
                    } else {
                        // 这个参数节点并没有找到，于是随便放点字符串进去
                        builder.addAll(Arrays.asList(TextComponent.fromLegacyText(text)));
                        TLib.getTLib().getLogger().warn(Strings.replaceWithOrder(TLib.getInternalLanguage().getString("MISSING-ARGUMENT"), node));
                    }
                    // 有可能一开头就是 <@xxx>，然后 split 出来就少了一些，于是直接加上
                    if (index < template.length) {
                        builder.addAll(Arrays.asList(TextComponent.fromLegacyText(template[index++])));
                    }
                }
                return builder.toArray(new BaseComponent[0]);
            }).collect(Collectors.toList());
            return new TLocaleJson(collect, isPlaceholderEnabled(map), map);
        }
        return new TLocaleJson(textList.stream().map(TextComponent::fromLegacyText).collect(Collectors.toList()), isPlaceholderEnabled(map), map);
    }

    private static List<String> getTextList(Object textObj) {
        if (textObj instanceof List) {
            return ((List<?>) textObj).stream().map(Object::toString).map(TLocale.Translate::setColored).collect(Collectors.toList());
        } else if (textObj instanceof String) {
            return Lists.newArrayList(TLocale.Translate.setColored((String) textObj));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        components.forEach(comp -> sendRawMessage(sender, replace(comp, sender, args)));
    }

    @Override
    public String asString(String... args) {
        return components.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        return Maps.newHashMap(map);
    }

    private void sendRawMessage(CommandSender sender, BaseComponent[] components) {
        TLocale.Tellraw.send(sender, ComponentSerializer.toString(components));
    }

    private List<BaseComponent> replace(List<BaseComponent> component, CommandSender sender, String... args) {
        return component.stream().map(c -> replace(c, sender, args)).collect(Collectors.toList());
    }

    private BaseComponent[] replace(BaseComponent[] component, CommandSender sender, String... args) {
        BaseComponent[] components = new BaseComponent[component.length];
        for (int i = 0; i < components.length; i++) {
            components[i] = replace(component[i].duplicate(), sender, args);
        }
        return components;
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
        if (component.getExtra() != null) {
            component.setExtra(replace(component.getExtra(), sender, args));
        }
        if (component instanceof TextComponent) {
            ((TextComponent) component).setText(replace(sender, ((TextComponent) component).getText(), args));
        }
        return component;
    }

    private String replace(CommandSender sender, String text, String[] args) {
        String s = Strings.replaceWithOrder(text, args);
        return papi ? PlaceholderHook.replace(sender, s) : s;
    }

    public static TellrawJson formatJson(Map<String, Object> section, Object textObject, TellrawJson pageJson) {
        List<String> textList = textObject instanceof List ? (List<String>) textObject : Collections.singletonList(String.valueOf(textObject));
        // 遍历本页文本
        for (int i = 0; i < textList.size(); i++) {
            // 捕捉变量
            for (VariableFormatter.Variable variable : new VariableFormatter(TLocale.Translate.setColored(textList.get(i)), pattern).find().getVariableList()) {
                // 如果是变量
                if (variable.isVariable()) {
                    String[] split = variable.getText().split("@");
                    // @ 前面的字符串
                    String text = split.length > 1 ? split[0] : "§4* Invalid Text *";
                    // @ 后面的节点名
                    String node = split.length > 1 ? split[1] : null;
                    // 处理变量
                    formatNode(section, pageJson, text, node);
                } else {
                    pageJson.append(variable.getText());
                }
            }
            if (i + 1 < textList.size()) {
                pageJson.newLine();
            }
        }
        return pageJson;
    }

    private static void formatNode(Map<String, Object> section, TellrawJson pageJson, String text, String node) {
        if (section.containsKey(node)) {
            try {
                Map<String, Object> args = (Map<String, Object>) section.get(node);
                // 文本
                pageJson.append(args.getOrDefault("text", text).toString());
                // 功能
                if (args.containsKey("item")) {
                    pageJson.hoverItem(ItemUtils.getCacheItem(args.get("item").toString()));
                }
                if (args.containsKey("hover")) {
                    pageJson.hoverText(args.get("hover").toString());
                }
                if (args.containsKey("suggest")) {
                    pageJson.clickSuggest(args.get("suggest").toString());
                }
                if (args.containsKey("command")) {
                    pageJson.clickCommand(args.get("command").toString());
                }
                if (args.containsKey("page")) {
                    pageJson.clickChangePage(NumberUtils.getInteger(args.get("page").toString()));
                }
                if (args.containsKey("url")) {
                    pageJson.clickOpenURL(args.get("url").toString());
                }
            } catch (Exception e) {
                TLocale.Logger.error("LOCALE.BOOK-ARGUMENTS-IDENTIFICATION-FAILED", e.toString());
            }
        } else {
            pageJson.append("§4* Invalid Argument *");
        }
    }
}
