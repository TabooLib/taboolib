package taboolib.common5;

import taboolib.common.Isolated;

import java.util.regex.Pattern;

/**
 * 物品Lore匹配工具
 *
 * @author YiMiner
 * @version 1.0
 * Aug 13, 2021
 */
@Isolated
public class ColorRegex {

    /**
     * 接受用户想要匹配的纯文本, 返回一个Pattern
     * 该Pattern在匹配物品Lore时能匹配一切的颜色
     * 由于能返回正确的匹配索引, 尤其适用于Lore替换相关功能
     *
     * @param regex 想要匹配的纯文本
     * @return 可直接使用的Pattern
     */
    public static Pattern patternIgnoreColor(String regex) {
        return Pattern.compile(stringToRegexIgnoreColor(regex), Pattern.CASE_INSENSITIVE);
    }

    /**
     * 接受用户想要匹配的纯文本, 返回一个正则串
     * 该正则串在匹配物品Lore时能匹配一切的颜色
     * 由于能返回正确的匹配索引, 尤其适用于Lore替换相关功能
     *
     * @param regex 想要匹配的纯文本
     * @return 新正则串
     */
    public static String stringToRegexIgnoreColor(String regex) {
        return stringToRegex(regex, "(?:§.)*", true);
    }

    /**
     * 接受用户想要匹配的纯文本, 返回一个新正则串
     * 效果等价于Pattern.quote(), 但不使用\Q和\E, 这意味着你可以自己编辑里面的内容
     *
     * @param str 想要匹配的纯文本
     */
    public static String stringToRegex(String str) {
        return stringToRegex(str, null, false);
    }

    /**
     * 接受用户想要匹配的纯文本, 返回新的正则串, 并在每个字符前插入指定的分隔符
     *
     * @param str         想要匹配的纯文本
     * @param delimiter   每个字符前的分隔符. 例如用(?:\\s*)可让表达式吞掉所有空格
     * @param ignoreColor 是否忽略掉原串里的颜色代码. 如果忽略, 生成的串将不含原串颜色代码
     * @return 新正则串
     */
    public static String stringToRegex(String str, String delimiter, boolean ignoreColor) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (ignoreColor && c == '§') {
                i++;
                continue;
            }
            if (delimiter != null) builder.append(delimiter);
            switch (c) {
                case ':':
                    builder.append("\\:");
                    break;
                case '+':
                    builder.append("\\+");
                    break;
                case '*':
                    builder.append("\\*");
                    break;
                case '?':
                    builder.append("\\?");
                    break;
                case '^':
                    builder.append("\\^");
                    break;
                case '$':
                    builder.append("\\$");
                    break;
                case '.':
                    builder.append("\\.");
                    break;
                case '(':
                    builder.append("\\(");
                    break;
                case ')':
                    builder.append("\\)");
                    break;
                case '[':
                    builder.append("\\[");
                    break;
                case ']':
                    builder.append("\\]");
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.toString();
    }
}