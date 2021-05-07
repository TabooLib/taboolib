package io.izzel.taboolib.module.locale;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 语言文件序列化接口
 *
 * @author sky
 * @since 2018-05-12 14:01
 */
public abstract class TLocaleSerialize implements TLocaleSender, ConfigurationSerializable {

    protected final boolean papi;
    protected final boolean kether;

    public TLocaleSerialize(boolean papi, boolean kether) {
        this.papi = papi;
        this.kether = kether;
    }

    public static boolean isKetherEnabled(Map<String, Object> map) {
        Object ketherObject = map.getOrDefault("kether", TLocale.Translate.isKetherUseDefault());
        return ketherObject instanceof Boolean ? (boolean) ketherObject : ketherObject instanceof String && "true".equals(ketherObject);
    }

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
        return new TLocaleSerialize(false, false) {

            @Override
            public void sendTo(CommandSender sender, String... args) {
            }

            @Override
            public Map<String, Object> serialize() {
                return null;
            }
        };
    }

    static TLocaleSerialize getEmpty(Plugin plugin, String path) {
        return new TLocaleSerialize(false, false) {

            @Override
            public Map<String, Object> serialize() {
                return null;
            }

            @Override
            public void sendTo(CommandSender sender, String... args) {
                if (TabooLibAPI.isDependTabooLib(plugin)) {
                    TLocaleLoader.sendTo(TabooLib.getPlugin(), path, sender, args);
                } else {
                    sender.sendMessage("<" + path + ">");
                }
            }

            @Override
            public String asString(String... args) {
                if (TabooLibAPI.isDependTabooLib(plugin)) {
                    return TLocaleLoader.asString(TabooLib.getPlugin(), path, args);
                } else {
                    return "<" + path + ">";
                }
            }

            @Override
            public List<String> asStringList(String... args) {
                if (TabooLibAPI.isDependTabooLib(plugin)) {
                    return TLocaleLoader.asStringList(TabooLib.getPlugin(), path, args);
                } else {
                    return Collections.singletonList("<" + path + ">");
                }
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
