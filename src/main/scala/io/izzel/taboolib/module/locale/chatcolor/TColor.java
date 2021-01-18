package io.izzel.taboolib.module.locale.chatcolor;

import io.izzel.taboolib.Version;
import io.izzel.taboolib.util.Coerce;
import io.izzel.taboolib.util.chat.ChatColor;

import java.awt.*;
import java.util.Optional;

/**
 * TabooLib
 * io.izzel.taboolib.module.locale.chatcolor.TColor
 *
 * @author sky
 * @since 2021/1/18 2:02 下午
 */
public class TColor {

    /**
     * 对字符串中的特殊颜色表达式进行转换
     * 可供转换的格式有：
     * &{255-255-255} —— RGB 代码
     * &{255,255,255} —— RGB 代码
     * &{#FFFFFF}      —— HEX 代码
     * &{BLUE}        —— 已知颜色（英文）
     * &{蓝}          —— 已知颜色（中文）
     *
     * @param in 字符串
     */
    public static String translate(String in) {
        String colored = ChatColor.translateAlternateColorCodes('&', in);
        // 1.16 supported
        if (Version.isAfter(Version.v1_16)) {
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
                        Optional<KnownColor> knownColor = KnownColor.matchKnownColor(toString(match));
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
            return builder.toString();
        }
        return colored;
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
        return Coerce.toInteger(builder.toString());
    }
}
