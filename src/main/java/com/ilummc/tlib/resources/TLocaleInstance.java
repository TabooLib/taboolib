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

    private final Plugin plugin;

    TLocaleInstance(Plugin plugin) {
        this.plugin = plugin;
    }

    void sendTo(String path, CommandSender sender, String... args) {
        try {
            map.getOrDefault(path, ImmutableList.of(TLocaleSendable.getEmpty(path))).forEach(sendable -> {
                if (Bukkit.isPrimaryThread()) {
                    sendable.sendTo(sender, args);
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> sendable.sendTo(sender, args));
                }
            });
        } catch (Exception e) {
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getTLib().getInternalLang().getString("SEND-LOCALE-ERROR"), path));
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getTLib().getInternalLang().getString("LOCALE-ERROR-REASON"), e.getMessage()));
        }
    }

    String asString(String path, String... args) {
        return map.getOrDefault(path, ImmutableList.of(TLocaleSendable.getEmpty(path))).get(0).asString(args);
    }

    void load(YamlConfiguration configuration) {
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

    int size() {
        return map.size();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    private static final Function<Object, TLocaleSendable> TO_SENDABLE = o -> {
        if (o instanceof TLocaleSendable) {
            return ((TLocaleSendable) o);
        } else if (o instanceof String) {
            return TLocaleText.of(((String) o));
        } else {
            return TLocaleText.of(String.valueOf(o));
        }
    };

    private final Map<String, List<TLocaleSendable>> map = new HashMap<>();

}
