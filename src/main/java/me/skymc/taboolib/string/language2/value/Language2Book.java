package me.skymc.taboolib.string.language2.value;

import com.ilummc.tlib.bungee.api.chat.BaseComponent;
import com.ilummc.tlib.bungee.api.chat.ClickEvent;
import com.ilummc.tlib.bungee.api.chat.HoverEvent;
import com.ilummc.tlib.bungee.api.chat.TextComponent;
import me.skymc.taboolib.bookformatter.BookFormatter;
import me.skymc.taboolib.bookformatter.action.ClickAction;
import me.skymc.taboolib.bookformatter.action.HoverAction;
import me.skymc.taboolib.bookformatter.builder.BookBuilder;
import me.skymc.taboolib.bookformatter.builder.PageBuilder;
import me.skymc.taboolib.bookformatter.builder.TextBuilder;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.string.VariableFormatter;
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
import java.util.regex.Pattern;

/**
 * @author sky
 * @since 2018-03-10 15:55:28
 */
public class Language2Book implements Language2Line {

    private static final String KEY_TEXT = "    text: ";
    private static final String KEY_COMMAND = "    command: ";
    private static final String KEY_SUGGEST = "    suggest: ";
    private static final String KEY_URL = "    url: ";
    private static final String KEY_PAGE = "    page: ";
    private static final String KEY_SHOWTEXT = "    showtext: ";
    private static final String KEY_SHOWITEM = "    showitem: ";
    private static final String KEY_OPTION = "@option:";
    private static final Pattern pattern = Pattern.compile("<@(\\S+)>");

    private Player player;

    private Language2Value value;

    private HashMap<String, TextBuilder> options = new HashMap<>();

    private BookBuilder book;

    public Language2Book(Language2Format format, List<String> list, Player player) {
        // 变量
        this.player = player;
        this.value = format.getLanguage2Value();
        this.book = BookFormatter.writtenBook();
        // 设置
        formatOptions(list);
        // 内容
        PageBuilder page = new PageBuilder();
        // 遍历内容
        for (String line : list) {
            // 翻页
            if (line.equals("[page]")) {
                book.addPages(page.build());
                page = new PageBuilder();
            }
            // 设置
            else if (line.startsWith("@option")) {
                break;
            } else {
                for (VariableFormatter.Variable variable : new VariableFormatter(line, pattern).find().getVariableList()) {
                    if (variable.isVariable()) {
                        String node = variable.getText().substring(1);
                        if (!options.containsKey(node)) {
                            page.add("§4[<ERROR-50: " + format.getLanguage2Value().getLanguageKey() + ">]");
                        } else {
                            TextBuilder builder = options.get(node);
                            BaseComponent component = new TextComponent(builder.getText());
                            if (builder.getHover() != null) {
                                component.setHoverEvent(new HoverEvent(builder.getHover().action(), builder.getHover().value()));
                            }
                            if (builder.getClick() != null) {
                                component.setClickEvent(new ClickEvent(builder.getClick().action(), builder.getClick().value()));
                            }
                            page.add(component);
                        }
                    } else {
                        page.add(variable.getText());
                    }
                }
                page.newLine();
            }
        }
        // 结尾
        book.addPages(page.build());
    }


    @Override
    public void send(Player player) {
        BookFormatter.forceOpen(player, book.build());
    }

    @Override
    public void console() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[<ERROR-40: " + value.getLanguageKey() + ">]");
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static Pattern getPattern() {
        return pattern;
    }

    public Player getPlayer() {
        return player;
    }

    public Language2Value getValue() {
        return value;
    }

    public HashMap<String, TextBuilder> getOptions() {
        return options;
    }

    public BookBuilder getBook() {
        return book;
    }

    // *********************************
    //
    //          Private Methods
    //
    // *********************************

    private List<List<String>> getBookPages(List<String> source) {
        List<List<String>> list = new ArrayList<>();
        for (String line : removeOption(source)) {
            if (line.equalsIgnoreCase("[page]")) {
                list.add(new ArrayList<>());
            } else {
                getLatestList(list).add(line);
            }
        }
        return list;
    }

    public List<String> getLatestList(List<List<String>> list) {
        if (list.size() == 0) {
            List<String> newList = new ArrayList<>();
            list.add(newList);
            return newList;
        } else {
            return list.get(list.size() - 1);
        }
    }

    public List<String> removeOption(List<String> source) {
        List<String> list = new ArrayList<>();
        for (String line : source) {
            if (!line.contains("@option")) {
                list.add(line);
            } else {
                return list;
            }
        }
        return list;
    }

    private void formatOptions(List<String> list) {
        // 获取书本设置
        HashMap<String, List<String>> _options = getOptions(list);
        for (Entry<String, List<String>> entry : _options.entrySet()) {
            TextBuilder builder = new TextBuilder();
            // 遍历内容
            for (String _option : entry.getValue()) {
                if (_option.startsWith(KEY_TEXT)) {
                    builder.text(_option.substring(KEY_TEXT.length()));
                } else if (_option.startsWith(KEY_COMMAND)) {
                    builder.onClick(ClickAction.runCommand(_option.substring(KEY_COMMAND.length())));
                } else if (_option.startsWith(KEY_SUGGEST)) {
                    builder.onClick(ClickAction.suggestCommand(_option.substring(KEY_SUGGEST.length())));
                } else if (_option.startsWith(KEY_URL)) {
                    try {
                        builder.onClick(ClickAction.openUrl(_option.substring(KEY_URL.length())));
                    } catch (Exception e) {
                        builder.text("§4[<ERROR-52: " + value.getLanguageKey() + ">]");
                    }
                } else if (_option.startsWith(KEY_PAGE)) {
                    builder.onClick(ClickAction.changePage(NumberUtils.getInteger(_option.substring(KEY_PAGE.length()))));
                } else if (_option.startsWith(KEY_SHOWTEXT)) {
                    builder.onHover(HoverAction.showText(_option.substring(KEY_SHOWTEXT.length())));
                } else if (_option.startsWith(KEY_SHOWITEM)) {
                    ItemStack item = ItemUtils.getCacheItem(_option.substring(KEY_SHOWITEM.length()));
                    if (item == null) {
                        item = new ItemStack(Material.STONE);
                    }
                    builder.onHover(HoverAction.showItem(item));
                }
            }
            options.put(entry.getKey(), builder);
        }
    }

    private HashMap<String, List<String>> getOptions(List<String> list) {
        HashMap<String, List<String>> options_source = new HashMap<>();
        List<String> option = new ArrayList<>();
        // 遍历
        String optionName = null;
        boolean start = false;
        // 遍历所有代码
        for (String line : list) {
            if (line.startsWith(KEY_OPTION)) {
                // 如果已经开始检测
                if (start) {
                    // 返回源码
                    options_source.put(optionName, new ArrayList<>(option));
                    // 清除源码
                    option.clear();
                }
                // 标签
                start = true;
                // 当前设置名称
                optionName = line.substring(KEY_OPTION.length());
            } else if (start) {
                option.add(line);
            }
        }
        // 返回最后设置
        options_source.put(optionName, option);
        return options_source;
    }
}
