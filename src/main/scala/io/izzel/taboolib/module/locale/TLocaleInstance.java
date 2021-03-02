package io.izzel.taboolib.module.locale;

import com.google.common.collect.ImmutableList;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.module.locale.type.*;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 预言文件实例
 */
@ThreadSafe
@SuppressWarnings("rawtypes")
class TLocaleInstance {

    private final Map<String, List<TLocaleSerialize>> map = new HashMap<>();
    private final Plugin plugin;
    private final AtomicInteger latestUpdateNodes = new AtomicInteger();

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

    public AtomicInteger getLatestUpdateNodes() {
        return latestUpdateNodes;
    }

    public void sendTo(String path, CommandSender sender, String... args) {
        try {
            map.getOrDefault(path, ImmutableList.of(TLocaleSerialize.getEmpty(plugin, path))).forEach(tSender -> {
                if (Bukkit.isPrimaryThread() || Objects.equals(System.getProperty("tlib.forceAsync"), "true")) {
                    tSender.sendTo(sender, args);
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> tSender.sendTo(sender, args));
                }
            });
        } catch (Exception | Error e) {
            TabooLib.getLogger().error(Strings.replaceWithOrder(TabooLib.getInternal().getString("SEND-LOCALE-ERROR"), path));
            TabooLib.getLogger().error(Strings.replaceWithOrder(TabooLib.getInternal().getString("LOCALE-ERROR-REASON"), e.toString()));
        }
    }

    public String asString(String path, String... args) {
        return map.getOrDefault(path, ImmutableList.of(TLocaleSerialize.getEmpty(plugin, path))).get(0).asString(args);
    }

    public List<String> asStringList(String path, String... args) {
        return map.getOrDefault(path, ImmutableList.of(TLocaleSerialize.getEmpty(plugin, path))).get(0).asStringList(args);
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
        load(configuration, false);
    }

    public void load(YamlConfiguration configuration, boolean cleanup) {
        int originNodes = map.size();
        int updateNodes = 0;
        if (cleanup) {
            map.clear();
        }
        for (String s : configuration.getKeys(true)) {
            boolean updated = false;
            Object value = configuration.get(s);
            if (value instanceof TLocaleSerialize) {
                updated = map.put(s, Collections.singletonList((TLocaleSerialize) value)) != null;
            }
            // TabooLib 4.x 兼容
            else if (TabooLibAPI.isOriginLoaded() && value.getClass().getName().startsWith("com.ilummc.tlib.resources.type")) {
                updated = map.put(s, Collections.singletonList(fromPluginVersion(value))) != null;
            } else if (value instanceof List && !((List) value).isEmpty()) {
                if (isListString((List) value)) {
                    updated = map.put(s, Collections.singletonList(TLocaleText.of(value))) != null;
                } else {
                    updated = map.put(s, ((List<?>) value).stream().map(o -> {
                        if (o instanceof TLocaleSerialize) {
                            return (TLocaleSerialize) o;
                        }
                        // TabooLib 4.x 兼容
                        else if (TabooLibAPI.isOriginLoaded() && o.getClass().getName().startsWith("com.ilummc.tlib.resources.type")) {
                            return fromPluginVersion(o);
                        }
                        return TLocaleText.of(String.valueOf(o));
                    }).collect(Collectors.toList())) != null;
                }
            } else if (!(value instanceof ConfigurationSection)) {
                String str = String.valueOf(value);
                updated = map.put(s, Collections.singletonList(str.length() == 0 ? TLocaleSerialize.getEmpty() : TLocaleText.of(str))) != null;
            }
            if (updated) {
                updateNodes++;
            }
        }
        latestUpdateNodes.set(originNodes - updateNodes);
    }

    private TLocaleSerialize fromPluginVersion(Object in) {
        switch (in.getClass().getSimpleName()) {
            case "TLocaleActionBar":
                return TLocaleActionBar.valueOf(TabooLibAPI.getPluginBridge().taboolibTLocaleSerialize(in));
            case "TLocaleBook":
                return TLocaleBook.valueOf(TabooLibAPI.getPluginBridge().taboolibTLocaleSerialize(in));
            case "TLocaleBossBar":
                return TLocaleBossBar.valueOf(TabooLibAPI.getPluginBridge().taboolibTLocaleSerialize(in));
            case "TLocaleJson":
                return TLocaleJson.valueOf(TabooLibAPI.getPluginBridge().taboolibTLocaleSerialize(in));
            case "TLocaleSound":
                return TLocaleSound.valueOf(TabooLibAPI.getPluginBridge().taboolibTLocaleSerialize(in));
            case "TLocaleTitle":
                return TLocaleTitle.valueOf(TabooLibAPI.getPluginBridge().taboolibTLocaleSerialize(in));
            default:
                return TLocaleText.valueOf(TabooLibAPI.getPluginBridge().taboolibTLocaleSerialize(in));
        }
    }
}
