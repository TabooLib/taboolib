package com.ilummc.tlib.resources;

import com.google.common.collect.ImmutableList;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.resources.type.TLocaleText;
import com.ilummc.tlib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ThreadSafe
@SuppressWarnings("rawtypes")
class TLocaleInstance {

    private final Map<String, List<TLocaleSerialize>> map = new HashMap<>();
    private final Plugin plugin;

    TLocaleInstance(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public int size() {
        return map.size();
    }

    public Map<String, List<TLocaleSerialize>> getMap() {
        return map;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void sendTo(String path, CommandSender sender, String... args) {
        try {
            map.getOrDefault(path, ImmutableList.of(TLocaleSerialize.getEmpty(path))).forEach(tSender -> {
                if (Bukkit.isPrimaryThread()) {
                    tSender.sendTo(sender, args);
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> tSender.sendTo(sender, args));
                }
            });
        } catch (Exception | Error e) {
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getInternalLanguage().getString("SEND-LOCALE-ERROR"), path));
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getInternalLanguage().getString("LOCALE-ERROR-REASON"), e.toString()));
            e.printStackTrace();
        }
    }

    public String asString(String path, String... args) {
        return map.getOrDefault(path, ImmutableList.of(TLocaleSerialize.getEmpty(path))).get(0).asString(args);
    }

    public List<String> asStringList(String path, String... args) {
        return map.getOrDefault(path, ImmutableList.of(TLocaleSerialize.getEmpty(path))).get(0).asStringList(args);
    }

    private static boolean isListString(List list) {
        for (Object o : list) {
            if (!(o instanceof String)) {
                return false;
            }
        }
        return true;
    }

    public void load(YamlConfiguration configuration) {
        configuration.getKeys(true).forEach(s -> {
            Object object = configuration.get(s);
            if (object instanceof TLocaleSerialize) {
                map.put(s, Collections.singletonList((TLocaleSerialize) object));
            } else if (object instanceof List && !((List) object).isEmpty()) {
                if (isListString((List) object)) {
                    map.put(s, Collections.singletonList(TLocaleText.of(object)));
                } else {
                    map.put(s, ((List<?>) object).stream().map(o -> o instanceof TLocaleSerialize ? (TLocaleSerialize) o : TLocaleText.of(String.valueOf(o))).collect(Collectors.toList()));
                }
            } else if (!(object instanceof ConfigurationSection)) {
                String str = String.valueOf(object);
                map.put(s, Collections.singletonList(str.length() == 0 ? TLocaleSerialize.getEmpty() : TLocaleText.of(str)));
            }
        });
    }
}
