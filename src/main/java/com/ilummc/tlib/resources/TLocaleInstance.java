package com.ilummc.tlib.resources;

import com.google.common.collect.ImmutableList;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.logger.TLogger;
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
    private int updateNodes;

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

    public int getUpdateNodes() {
        return updateNodes;
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
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getTLib().getInternalLanguage().getString("SEND-LOCALE-ERROR"), path));
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getTLib().getInternalLanguage().getString("LOCALE-ERROR-REASON"), e.toString()));
            e.printStackTrace();
        }
    }

    public String asString(String path, String... args) {
        return map.getOrDefault(path, ImmutableList.of(TLocaleSendable.getEmpty(path))).get(0).asString(args);
    }

    public List<String> asStringList(String path, String... args) {
        return map.getOrDefault(path, ImmutableList.of(TLocaleSendable.getEmpty(path))).get(0).asStringList(args);
    }

    public void load(YamlConfiguration configuration) {
        updateNodes = 0;
        configuration.getKeys(true).forEach(s -> {
            boolean isCover = false;
            Object object = configuration.get(s);
            if (object instanceof TLocaleSendable) {
                isCover = map.put(s, Collections.singletonList((TLocaleSendable) object)) != null;
            } else if (object instanceof List && !((List) object).isEmpty()) {
                isCover = map.put(s, ((List<?>) object).stream().map(TO_SENDABLE).collect(Collectors.toList())) != null;
            } else if (!(object instanceof ConfigurationSection)) {
                String str = String.valueOf(object);
                isCover = map.put(s, Collections.singletonList(str.length() == 0 ? TLocaleSendable.getEmpty() : TLocaleText.of(str))) != null;
            }
            if (isCover) {
                updateNodes++;
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
}
