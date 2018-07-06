package me.skymc.taboolib.json;

import java.util.regex.Pattern;

/**
 * @Author sky
 * @Since 2018-07-01 11:10
 */
public class JSONReader {

    private static Pattern pattern = Pattern.compile("[\t\n]");

    public static String formatJson(String content) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        int count = 0;
        while (index < content.length()) {
            char ch = content.charAt(index);
            if (ch == '{' || ch == '[') {
                builder.append(ch);
                builder.append('\n');
                count++;
                for (int i = 0; i < count; i++) {
                    builder.append('\t');
                }
            } else if (ch == '}' || ch == ']') {
                builder.append('\n');
                count--;
                for (int i = 0; i < count; i++) {
                    builder.append('\t');
                }
                builder.append(ch);
            } else if (ch == ',') {
                builder.append(ch);
                builder.append('\n');
                for (int i = 0; i < count; i++) {
                    builder.append('\t');
                }
            } else {
                builder.append(ch);
            }
            index++;
        }
        return compactJson(builder.toString());
    }

    private static String compactJson(String content) {
        return pattern.matcher(content).replaceAll("").trim();
    }
}
