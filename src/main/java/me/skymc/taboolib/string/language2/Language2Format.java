package me.skymc.taboolib.string.language2;

import me.skymc.taboolib.string.language2.value.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author sky
 * @since 2018-03-08 22:45:56
 */
public class Language2Format implements Language2Line {

    private Language2Value language2Value;
    private List<Language2Line> language2Lines = new ArrayList<>();

    public Language2Format(Player player, Language2Value value) {
        language2Value = value;
        // 语言类型
        Language2Type type = Language2Type.TEXT;
        // 递交数据
        List<String> values = new LinkedList<>();

        // 遍历内容
        for (String line : value.getLanguageValue()) {
            // 文本类型
            if (line.contains("[text]")) {
                // 递交数据
                parseValue(player, values, type);
                // 更改类型
                type = Language2Type.TEXT;
            }
            // 大标题
            else if (line.contains("[title]")) {
                // 递交数据
                parseValue(player, values, type);
                // 更改类型
                type = Language2Type.TITLE;
            }
            // 小标题
            else if (line.contains("[action]")) {
                // 递交数据
                parseValue(player, values, type);
                // 更改类型
                type = Language2Type.ACTION;
            }
            // JSON
            else if (line.contains("[json]")) {
                // 递交数据
                parseValue(player, values, type);
                // 更改类型
                type = Language2Type.JSON;
            }
            // JSON2
            else if (line.contains("[json2]")) {
                // 递交数据
                parseValue(player, values, type);
                // 更改类型
                type = Language2Type.JSON2;
            }
            // 音效
            else if (line.contains("[sound]")) {
                // 递交数据
                parseValue(player, values, type);
                // 更改类型
                type = Language2Type.SOUND;
            }
            // 书本
            else if (line.contains("[book]")) {
                // 递交数据
                parseValue(player, values, type);
                // 更改类型
                type = Language2Type.BOOK;
            } else if (line.contains("[return]")) {
                // 递交数据
                parseValue(player, values, type);
            }
            // 默认
            else {
                // 追加内容
                values.add(line);
            }
        }
    }

    public Language2Value getLanguage2Value() {
        return language2Value;
    }

    public List<Language2Line> getLanguage2Lines() {
        return language2Lines;
    }

    /**
     * 识别内容
     *
     * @param player 玩家
     * @param list   数据
     * @param type   类型
     */
    private void parseValue(Player player, List<String> list, Language2Type type) {
        if (list.size() == 0) {
            return;
        }
        // 变量转换
        List<String> listPlaceholder = language2Value.setPlaceholder(list, player);
        // 大标题
        switch (type) {
            case TITLE:
                language2Lines.add(new Language2Title(this, listPlaceholder));
                break;
            // 小标题
            case ACTION:
                language2Lines.add(new Language2Action(this, listPlaceholder));
                break;
            // JSON
            case JSON:
                language2Lines.add(new Language2Json(this, listPlaceholder, player));
                break;
            // JSON2
            case JSON2:
                language2Lines.add(new Language2Json2(this, listPlaceholder, player));
                break;
            // 音效
            case SOUND:
                language2Lines.add(new Language2Sound(this, listPlaceholder));
                break;
            // 书本
            case BOOK:
                language2Lines.add(new Language2Book(this, listPlaceholder, player));
                break;
            default:
                language2Lines.add(new Language2Text(this, listPlaceholder));
                break;
        }
        // 清理数据
        list.clear();
        listPlaceholder.clear();
    }

    @Override
    public void send(Player player) {
        for (Language2Line line : language2Lines) {
            line.send(player);
        }
    }

    @Override
    public void console() {
        for (Language2Line line : language2Lines) {
            line.console();
        }
    }
}
