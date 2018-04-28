package com.ilummc.tlib.resources;

import com.google.common.collect.ImmutableList;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.resources.type.TLocaleText;
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
            TLib.getTLib().getLogger().error("语言文件发送失败: " + path);
            TLib.getTLib().getLogger().error("原因: " + e.getMessage());
        }
    }

    String asString(String path) {
        return map.getOrDefault(path, ImmutableList.of(TLocaleSendable.getEmpty(path))).get(0).asString();
    }

    void load(YamlConfiguration configuration) {
        configuration.getKeys(false).forEach(s -> {
            Object object = configuration.get(s);
            if (object instanceof ConfigurationSection) {
                loadRecursively(s, (ConfigurationSection) object);
            } else if (object instanceof TLocaleSendable) {
                map.put(s, Collections.singletonList((TLocaleSendable) object));
            } else if (object instanceof List && !((List) object).isEmpty()) {
                map.put(s, ((List<?>) object).stream().map(TO_SENDABLE).collect(Collectors.toList()));
            } else {
                map.put(s, Collections.singletonList(TLocaleText.of(String.valueOf(object))));
            }
        });
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

    private void loadRecursively(String path, ConfigurationSection section) {
        section.getKeys(false).forEach(s -> {
            Object object = section.get(path + "." + s);
            if (object instanceof ConfigurationSection) {
                loadRecursively(path + "." + s, (ConfigurationSection) object);
            } else if (object instanceof TLocaleSendable) {
                map.put(path + "." + s, Collections.singletonList((TLocaleSendable) object));
            } else if (object instanceof List && !((List) object).isEmpty()) {
                map.put(path + "." + s, ((List<?>) object).stream().map(TO_SENDABLE).collect(Collectors.toList()));
            } else {
                map.put(path + "." + s, Collections.singletonList(TLocaleText.of(String.valueOf(object))));
            }
        });
    }

}
