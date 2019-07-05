package io.izzel.taboolib.locale;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Author sky
 * @Since 2018-05-12 14:01
 */
public abstract class TLocaleSerialize implements TLocaleSender, ConfigurationSerializable {

    public static boolean isPlaceholderEnabled(Map<String, Object> map) {
        Object placeholderObject = map.getOrDefault("papi", TLocale.Translate.isPlaceholderUseDefault());
        return placeholderObject instanceof Boolean ? (boolean) placeholderObject : placeholderObject instanceof String && "true".equals(placeholderObject);
    }

    public static String getStringOrDefault(Map<String, Object> map, String path, String def) {
        Object var = map.getOrDefault(path, def);
        return var instanceof String ? (String) var : def;
    }

    public static Integer getIntegerOrDefault(Map<String, Object> map, String path, Integer def) {
        Object var = map.getOrDefault(path, def);
        return var instanceof Integer ? (Integer) var : def;
    }

    public static Double getDoubleOrDefault(Map<String, Object> map, String path, Double def) {
        Object var = map.getOrDefault(path, def);
        return var instanceof Double ? (Double) var : def;
    }

    static TLocaleSerialize getEmpty() {
        return new TLocaleSerialize() {

            @Override
            public void sendTo(CommandSender sender, String... args) {
            }

            @Override
            public Map<String, Object> serialize() {
                return null;
            }
        };
    }

    static TLocaleSerialize getEmpty(String path) {
        return new TLocaleSerialize() {

            @Override
            public Map<String, Object> serialize() {
                return null;
            }

            @Override
            public void sendTo(CommandSender sender, String... args) {
                sender.sendMessage("§8<" + path + "§8>");
            }

            @Override
            public String asString(String... args) {
                return "§8<" + path + "§8>";
            }

            @Override
            public List<String> asStringList(String... args) {
                return Collections.singletonList("§4<" + path + "§4>");
            }
        };
    }

    @Override
    public String asString(String... args) {
        return "";
    }

    @Override
    public List<String> asStringList(String... args) {
        return Collections.emptyList();
    }
}
