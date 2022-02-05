package taboolib.module.chat;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Optional;

/**
 * @author sky
 * @since 2021/1/18 2:02 下午
 */
public class ColorTranslator {

    private static boolean isLegacy = false;

    static {
        try {
            ChatColor.of(Color.BLACK);
        } catch (NoSuchMethodError ignored) {
            isLegacy = true;
        }
    }

    /**
     * 对字符串中的特殊颜色表达式进行转换<br>
     * 可供转换的格式有：
     * <p>
     * &amp;{255-255-255} —— RGB 代码
     * <p>
     * &amp;{255,255,255} —— RGB 代码
     * <p>
     * &amp;{#FFFFFF}     —— HEX 代码
     * <p>
     * &amp;{BLUE}        —— 已知颜色（英文）
     * <p>
     * &amp;{蓝}          —— 已知颜色（中文）
     *
     * @param in 字符串
     * @return String
     */
    @NotNull
    public static String translate(String in) {
        if (isLegacy) {
            return ChatColor.translateAlternateColorCodes('&', in);
        }
        StringBuilder builder = new StringBuilder();
        char[] chars = in.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i + 1 < chars.length && chars[i] == '&' && chars[i + 1] == '{') {
                ChatColor chatColor = null;
                char[] match = new char[0];
                for (int j = i + 2; j < chars.length && chars[j] != '}'; j++) {
                    match = arrayAppend(match, chars[j]);
                }
                if (match.length == 11 && (match[3] == ',' || match[3] == '-') && (match[7] == ',' || match[7] == '-')) {
                    chatColor = ChatColor.of(new Color(toInt(match, 0, 3), toInt(match, 4, 7), toInt(match, 8, 11)));
                } else if (match.length == 7 && match[0] == '#') {
                    try {
                        chatColor = ChatColor.of(toString(match));
                    } catch (IllegalArgumentException ignored) {
                    }
                } else {
                    Optional<Colors> knownColor = Colors.matchKnownColor(toString(match));
                    if (knownColor.isPresent()) {
                        chatColor = knownColor.get().toChatColor();
                    }
                }
                if (chatColor != null) {
                    builder.append(chatColor);
                    i += match.length + 2;
                }
            } else {
                builder.append(chars[i]);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }

    private static char[] arrayAppend(char[] chars, char in) {
        char[] newChars = new char[chars.length + 1];
        System.arraycopy(chars, 0, newChars, 0, chars.length);
        newChars[chars.length] = in;
        return newChars;
    }

    private static String toString(char[] chars) {
        StringBuilder builder = new StringBuilder();
        for (char c : chars) {
            builder.append(c);
        }
        return builder.toString();
    }

    private static int toInt(char[] chars, int start, int end) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            builder.append(chars[i]);
        }
        return Integer.parseInt(builder.toString());
    }
}
