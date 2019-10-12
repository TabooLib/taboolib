package io.izzel.taboolib.util;

import com.google.common.collect.Maps;
import io.izzel.taboolib.util.lite.Numbers;
import org.bukkit.util.NumberConversions;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author sky
 * @Since 2019-10-06 17:18
 */
public class TMap {

    private String name;
    private Map<String, String> content = Maps.newHashMap();

    public TMap(String name) {
        this.name = name;
    }

    public int getInt(String... key) {
        return getInt(key, 0);
    }

    public int getInt(String[] key, int def) {
        return Arrays.stream(key).filter(content::containsKey).mapToInt(i -> NumberConversions.toInt(content.get(i))).findFirst().orElse(def);
    }

    public double getDouble(String... key) {
        return getDouble(key, 0);
    }

    public double getDouble(String[] key, double def) {
        return Arrays.stream(key).filter(content::containsKey).mapToDouble(i -> NumberConversions.toDouble(content.get(i))).findFirst().orElse(def);
    }

    public boolean getBoolean(String... key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String[] key, boolean def) {
        return Arrays.stream(key).filter(content::containsKey).map(i -> Numbers.getBoolean(content.get(i))).findFirst().orElse(def);
    }

    public String get(String... key) {
        return get(key, null);
    }

    public String get(String[] key, String def) {
        return Arrays.stream(key).filter(content::containsKey).map(i -> content.get(i)).findFirst().orElse(def);
    }

    public static TMap parse(String in) {
        Matcher matcher = Pattern.compile("(?<name>[^{}]+)?\\{(?<content>[^<>]+)}").matcher(in.replaceAll("[\r\n]", ""));
        if (matcher.find()) {
            TMap map = new TMap(matcher.group("name"));
            for (String content : matcher.group("content").split(";")) {
                String[] v = parsePair(content);
                if (v.length == 2) {
                    map.content.put(v[0].toLowerCase().trim(), v[1].trim());
                }
            }
            return map;
        }
        return new TMap(null);
    }

    public static String[] parsePair(String in) {
        String[] v = in.split("=");
        StringBuilder r = new StringBuilder();
        for (int i = 1; i < v.length; i++) {
            if (i > 1) {
                r.append("=");
            }
            r.append(v[i]);
        }
        return new String[] {v[0], r.toString().replace("`", "")};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TMap)) {
            return false;
        }
        TMap tMap = (TMap) o;
        return Objects.equals(getName(), tMap.getName()) &&
                Objects.equals(getContent(), tMap.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getContent());
    }

    @Override
    public String toString() {
        return "TMap{" +
                "name='" + name + '\'' +
                ", content=" + content +
                '}';
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public String getName() {
        return name;
    }

    public Map<String, String> getContent() {
        return content;
    }
}
