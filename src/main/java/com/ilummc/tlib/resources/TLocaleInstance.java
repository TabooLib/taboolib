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

    private final Map<String, List<TLocaleSendable>> map = new HashMap<>();
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

    public Map<String, List<TLocaleSendable>> getMap() {
        return map;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void sendTo(String path, CommandSender sender, String... args) {
        try {
            map.getOrDefault(path, ImmutableList.of(TLocaleSendable.getEmpty(path))).forEach(sendable -> {
                if (Bukkit.isPrimaryThread()) {
                    sendable.sendTo(sender, args);
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> sendable.sendTo(sender, args));
                }
            });
        } catch (Exception | Error e) {
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getInternalLanguage().getString("SEND-LOCALE-ERROR"), path));
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getInternalLanguage().getString("LOCALE-ERROR-REASON"), e.toString()));
            e.printStackTrace();
        }
    }

    public String asString(String path, String... args) {
        return map.getOrDefault(path, ImmutableList.of(TLocaleSendable.getEmpty(path))).get(0).asString(args);
    }

    public List<String> asStringList(String path, String... args) {
        return map.getOrDefault(path, ImmutableList.of(TLocaleSendable.getEmpty(path))).get(0).asStringList(args);
    }

    private static final Function<Object, TLocaleSendable> TO_SENDABLE = o -> {
        if (o instanceof TLocaleSendable) {
            return ((TLocaleSendable) o);
        } else if (o instanceof String || (o instanceof List && isListString(((List) o)))) {
            return TLocaleText.of(o);
        } else {
            return TLocaleText.of(String.valueOf(o));
        }
    };

    private static boolean isListString(List list) {
        for (Object o : list) {
            if (!(o instanceof String)) return false;
        }
        return true;
    }

    public void load(YamlConfiguration configuration) {
        configuration.getKeys(true).forEach(s -> {
            Object object = configuration.get(s);
            if (object instanceof TLocaleSendable) {
                map.put(s, Collections.singletonList((TLocaleSendable) object));
            } else if (object instanceof List && !((List) object).isEmpty()) {
                map.put(s, ((List<?>) object).stream().map(TO_SENDABLE).collect(Collectors.toList()));
            } else if (!(object instanceof ConfigurationSection)) {
                String str = String.valueOf(object);
                map.put(s, Collections.singletonList(str.length() == 0 ? TLocaleSendable.getEmpty() : TLocaleText.of(str)));
            }
        });
    }
}
