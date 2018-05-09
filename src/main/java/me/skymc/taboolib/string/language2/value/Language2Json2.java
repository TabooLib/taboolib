package me.skymc.taboolib.string.language2.value;

import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.ClickEvent;
import me.skymc.taboolib.jsonformatter.click.OpenUrlEvent;
import me.skymc.taboolib.jsonformatter.click.RunCommandEvent;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.HoverEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowItemEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;
import me.skymc.taboolib.string.language2.Language2Format;
import me.skymc.taboolib.string.language2.Language2Line;
import me.skymc.taboolib.string.language2.Language2Value;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sky
 * @since 2018-03-10 15:55:28
 */
public class Language2Json2 implements Language2Line {

    private static final String KEY_TEXT = "    text: ";
    private static final String KEY_COMMAND = "    command: ";
    private static final String KEY_SUGGEST = "    suggest: ";
    private static final String KEY_URL = "    url: ";
    private static final String KEY_SHOWTEXT = "    showtext: ";
    private static final String KEY_SHOWITEM = "    showitem: ";
    private static final String KEY_OPTION = "@option:";
    private static final Pattern pattern = Pattern.compile("<@(\\S+)>");

    private Player player;

    private Language2Value value;

    private HashMap<String, JSONFormatter> options = new HashMap<>();

    private JSONFormatter json = new JSONFormatter();

    public Language2Json2(Language2Format format, List<String> list, Player player) {
        // 变量
        this.player = player;
        this.value = format.getLanguage2Value();

        // 获取书本设置
        formatOptions(list);
        // 遍历内容
        int lineNumber = 0;
        int lineNumberEnd = getLineNumberEnd(list);
        for (String line : list) {
            if (line.startsWith("@option")) {
                break;
            } else {
                Matcher matcher = pattern.matcher(line);
                boolean find = false;
                while (matcher.find()) {
                    find = true;
                    String optionName = matcher.group(1);
                    String optionFullName = "<@" + matcher.group(1) + ">";
                    // 判断设置是否存在
                    if (!options.containsKey(optionName)) {
                        json.append("§4[<ERROR-60: " + format.getLanguage2Value().getLanguageKey() + ">]");
                    } else {
                        String[] line_split = line.split(optionFullName);
                        try {
                            // 单独一行
                            if (line_split.length == 0) {
                                json.append(options.get(optionName));
                            } else {
                                // 前段
                                json.append(line_split[0]);
                                // 变量
                                json.append(options.get(optionName));
                                // 后段
                                if (line_split.length >= 2) {
                                    // 获取文本
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 1; i < line_split.length; i++) {
                                        sb.append(line_split[i]).append(optionFullName);
                                    }
                                    // 更改文本
                                    line = sb.substring(0, sb.length() - optionFullName.length());
                                    // 如果后段还有变量
                                    if (!pattern.matcher(line).find()) {
                                        json.append(line_split[1]);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            json.append("§4[<ERROR-61: " + format.getLanguage2Value().getLanguageKey() + ">]");
                        }
                    }
                }
                if (!find) {
                    json.append(line);
                }
                if (++lineNumber < lineNumberEnd) {
                    json.newLine();
                }
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Language2Value getValue() {
        return value;
    }

    public HashMap<String, JSONFormatter> getOptions() {
        return options;
    }

    public JSONFormatter getJson() {
        return json;
    }

    private int getLineNumberEnd(List<String> list) {
        int line = list.size();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).startsWith("@option")) {
                return i;
            }
        }
        return line;
    }

    private void formatOptions(List<String> list) {
        HashMap<String, List<String>> _options = getOptions(list);
        for (Entry<String, List<String>> entry : _options.entrySet()) {
            JSONFormatter jsonFormatter = new JSONFormatter();
            String current = ChatColor.DARK_RED + "[<ERROR-20: " + value.getLanguageKey() + ">]";
            ClickEvent clickEvent = null;
            HoverEvent hoverEvent = null;
            for (String _option : entry.getValue()) {
                if (_option.startsWith(KEY_TEXT)) {
                    current = _option.substring(KEY_TEXT.length());
                } else if (_option.startsWith(KEY_COMMAND)) {
                    clickEvent = new RunCommandEvent(_option.substring(KEY_COMMAND.length()));
                } else if (_option.startsWith(KEY_SUGGEST)) {
                    clickEvent = new SuggestCommandEvent(_option.substring(KEY_SUGGEST.length()));
                } else if (_option.startsWith(KEY_URL)) {
                    clickEvent = new OpenUrlEvent(_option.substring(KEY_URL.length()));
                } else if (_option.startsWith(KEY_SHOWTEXT)) {
                    hoverEvent = new ShowTextEvent(_option.replace("||", "\n").substring(KEY_SHOWTEXT.length()));
                } else if (_option.startsWith(KEY_SHOWITEM)) {
                    ItemStack item = ItemUtils.getCacheItem(_option.substring(KEY_SHOWITEM.length()));
                    if (item == null) {
                        item = new ItemStack(Material.STONE);
                    }
                    hoverEvent = new ShowItemEvent(item);
                }
            }
            append(jsonFormatter, current, clickEvent, hoverEvent);
            options.put(entry.getKey(), jsonFormatter);
        }
    }

    private void append(JSONFormatter json, String current, ClickEvent clickEvent, HoverEvent hoverEvent) {
        if (clickEvent == null && hoverEvent == null) {
            json.append(current);
        } else if (clickEvent != null && hoverEvent == null) {
            json.appendClick(current, clickEvent);
        } else if (clickEvent == null) {
            json.appendHover(current, hoverEvent);
        } else {
            json.appendHoverClick(current, hoverEvent, clickEvent);
        }
    }

    private HashMap<String, List<String>> getOptions(List<String> list) {
        HashMap<String, List<String>> options_source = new HashMap<>();
        List<String> option = new ArrayList<>();
        String optionName = null;
        boolean start = false;
        for (String line : list) {
            if (line.startsWith(KEY_OPTION)) {
                if (start) {
                    options_source.put(optionName, new ArrayList<>(option));
                    option.clear();
                }
                start = true;
                optionName = line.substring(KEY_OPTION.length());
            } else if (start) {
                option.add(line);
            }
        }
        options_source.put(optionName, option);
        return options_source;
    }

    @Override
    public void send(Player player) {
        json.send(player);
    }

    @Override
    public void console() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[<ERROR-40: " + value.getLanguageKey() + ">]");
    }
}
