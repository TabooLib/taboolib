package me.skymc.taboolib.string.language2;

import me.skymc.taboolib.string.language2.value.Language2Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author sky
 * @since 2018年2月13日 下午3:05:15
 */
public class Language2Value {

    private Language2 language;

    private String languageKey;

    private List<String> languageValue;

    private LinkedHashMap<String, String> placeholder = new LinkedHashMap<>();

    private boolean enablePlaceholderAPI = false;

    /**
     * 构造方法
     */
    public Language2Value(Language2 language, String languageKey) {
        // 如果语言文件不存在
        if (language == null || languageKey == null) {
            languageValue = Arrays.asList(ChatColor.DARK_RED + "[<ERROR-0>]", "[return]");
            return;
        }

        // 如果语言文本不存在
        if (!language.getConfiguration().contains(languageKey)) {
            languageValue = Arrays.asList(ChatColor.DARK_RED + "[<ERROR-1: " + languageKey + ">]", "[return]");
            return;
        }

        // 如果不是集合类型
        if (language.getConfiguration().get(languageKey) instanceof List) {
            // 设置文本
            languageValue = asColored(language.getConfiguration().getStringList(languageKey));
            // 追加结尾
            languageValue.add("[return]");
            // 是否启用PAPI
            if (languageValue.get(0).contains("[papi]")) {
                enablePlaceholderAPI = true;
            }
        } else {
            // 设置文本
            languageValue = Arrays.asList(ChatColor.translateAlternateColorCodes('&', language.getConfiguration().getString(languageKey)), "[return]");
        }

        // 初始化变量
        this.language = language;
        this.languageKey = languageKey;
    }

    public Language2 getLanguage() {
        return language;
    }

    public String getLanguageKey() {
        return languageKey;
    }

    public List<String> getLanguageValue() {
        return languageValue;
    }

    public LinkedHashMap<String, String> getPlaceholder() {
        return placeholder;
    }

    public boolean isEnablePlaceholderAPI() {
        return enablePlaceholderAPI;
    }

    /**
     * 向玩家发送信息
     *
     * @param player
     */
    public void send(Player player) {
        new Language2Format(player, this).send(player);
    }

    /**
     * 向玩家发送信息
     *
     * @param players 玩家
     */
    public void send(List<Player> players) {
        for (Player player : players) {
            send(player);
        }
    }

    /**
     * 向指令发送者发送信息
     *
     * @param sender
     */
    public void send(CommandSender sender) {
        if (sender instanceof Player) {
            send((Player) sender);
        } else {
            console();
        }
    }

    /**
     * 全服公告
     */
    public void broadcast() {
        send(new ArrayList<>(Bukkit.getOnlinePlayers()));
    }

    /**
     * 发送到后台
     */
    public void console() {
        new Language2Format(null, this).console();
    }

    /**
     * 获取文本
     *
     * @return
     */
    public String asString() {
        Language2Format format = new Language2Format(null, this);
        if (format.getLanguage2Lines().get(0) instanceof Language2Text) {
            Language2Text text = (Language2Text) format.getLanguage2Lines().get(0);
            return setPlaceholder(text.getText().get(0), null);
        } else {
            return languageValue.size() == 0 ? ChatColor.DARK_RED + "[<ERROR-1>]" : setPlaceholder(languageValue.get(0), null);
        }
    }

    /**
     * 获取文本集合
     *
     * @return
     */
    public List<String> asStringList() {
        Language2Format format = new Language2Format(null, this);
        if (format.getLanguage2Lines().get(0) instanceof Language2Text) {
            Language2Text text = (Language2Text) format.getLanguage2Lines().get(0);
            return setPlaceholder(text.getText(), null);
        } else {
            return Collections.singletonList(languageValue.size() == 0 ? ChatColor.DARK_RED + "[<ERROR-1>]" : setPlaceholder(languageValue.get(0), null));
        }
    }

    /**
     * 变量替换
     *
     * @param value  替换文本
     * @param player 检测玩家
     * @return String
     */
    public String setPlaceholder(String value, Player player) {
        for (Entry<String, String> entry : placeholder.entrySet()) {
            value = value.replace(entry.getKey(), entry.getValue());
        }
        return isEnablePlaceholderAPI() ? this.language.setPlaceholderAPI(player, value) : value;
    }

    /**
     * 变量替换
     *
     * @param list   替换集合
     * @param player 检测玩家
     * @return {@link List}
     */
    public List<String> setPlaceholder(List<String> list, Player player) {
        List<String> _list = new ArrayList<>(list);
        for (int i = 0; i < _list.size(); i++) {
            _list.set(i, setPlaceholder(_list.get(i), player));
        }
        return _list;
    }

    /**
     * 变量替换构造
     *
     * @param key   键
     * @param value 值
     * @return {@link Language2Value}
     */
    public Language2Value addPlaceholder(String key, String value) {
        this.placeholder.put(key, value);
        return this;
    }

    /**
     * 替换颜色
     *
     * @param list
     * @return
     */
    public List<String> asColored(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            list.set(i, ChatColor.translateAlternateColorCodes('&', list.get(i)));
        }
        return list;
    }

    @Override
    public String toString() {
        return asString();
    }
}
