package taboolib.module.chat;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Optional;

/**
 * @author sky
 * @since 2021/1/18 2:02 下午
 */
public class HexColor {

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
    @SuppressWarnings("CallToPrintStackTrace")
    @NotNull
    public static String translate(String in) {
        if (isLegacy) {
            return ChatColor.translateAlternateColorCodes('&', in);
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            if (i + 1 < in.length() && in.charAt(i) == '&' && in.charAt(i + 1) == '{') {
                int end = in.indexOf('}', i + 2);
                if (end >= 0) {
                    String expression = in.substring(i + 2, end).trim();
                    Optional<StandardColors> knownColor = StandardColors.match(expression);
                    Integer color = parseColor(expression);
                    if (knownColor.isPresent()) {
                        builder.append(knownColor.get().toChatColor());
                        i = end;
                        continue;
                    } else if (color != null) {
                        builder.append(ChatColor.of(new Color(color)));
                        i = end;
                        continue;
                    }
                }
            }
            builder.append(in.charAt(i));
        }
        String colorString = builder.toString();
        // 1.20.4 不再支持该写法，该模块无法判断版本，因此全部替换为白色
        // 若需要恢复默认色请使用 SimpleComponent 中的 reset 属性
        colorString = colorString.replace("&r", "&f").replace("§r", "§f");
        return ChatColor.translateAlternateColorCodes('&', colorString);
    }

    public static String getColorCode(int color) {
        return ChatColor.of(new Color(color)).toString();
    }

    @Nullable
    static Integer parseColor(String source) {
        String value = source.trim();
        if (value.matches("#[0-9a-fA-F]{6}")) {
            return Integer.parseInt(value.substring(1), 16);
        }
        Character separator = null;
        if (value.indexOf(',') >= 0) {
            separator = ',';
        } else if (value.indexOf('-') >= 0) {
            separator = '-';
        }
        if (separator != null) {
            String[] parts = value.split("\\" + separator, -1);
            if (parts.length != 3) {
                return null;
            }
            int color = 0;
            for (String part : parts) {
                int component;
                try {
                    component = Integer.parseInt(part.trim());
                } catch (NumberFormatException ignored) {
                    return null;
                }
                if (component < 0 || component > 255) {
                    return null;
                }
                color = color << 8 | component;
            }
            return color;
        }
        Optional<StandardColors> knownColor = StandardColors.match(value);
        if (knownColor.isPresent() && knownColor.get().toChatColor().getColor() != null) {
            return knownColor.get().toChatColor().getColor().getRGB() & 0xFFFFFF;
        }
        return null;
    }
}
