package com.ilummc.tlib.resources;

import com.google.common.collect.ImmutableList;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@ThreadSafe
class LocaleInstance {

    private static final Function<Object, Sendable> TO_SENDABLE = o -> {
        if (o instanceof Sendable) return ((Sendable) o);
        else if (o instanceof String) return SimpleChatMessage.of(((String) o));
        else return SimpleChatMessage.of(String.valueOf(o));
    };

    LocaleInstance() {
    }

    private final Map<String, List<Sendable>> map = new ConcurrentHashMap<>();

    void sendTo(String path, CommandSender sender) {
        map.getOrDefault(path, ImmutableList.of(Sendable.EMPTY)).forEach(sendable -> sendable.sendTo(sender));
    }

    void sendTo(String path, CommandSender sender, String... args) {
        map.getOrDefault(path, ImmutableList.of(Sendable.EMPTY)).forEach(sendable -> sendable.sendTo(sender, args));
        System.out.println(map.toString());
    }

    void load(YamlConfiguration configuration) {
        configuration.getKeys(false).forEach(s -> {
            Object object = configuration.get(s);
            if (object instanceof ConfigurationSection)
                loadRecursively(s, (ConfigurationSection) object);
            else if (object instanceof Sendable)
                map.put(s, Collections.singletonList((Sendable) object));
            else if (object instanceof List && !((List) object).isEmpty())
                map.put(s, ((List<?>) object).stream().map(TO_SENDABLE).collect(Collectors.toList()));
            else map.put(s, Collections.singletonList(SimpleChatMessage.of(String.valueOf(object))));
        });
    }

    private void loadRecursively(String path, ConfigurationSection section) {
        section.getKeys(false).forEach(s -> {
            Object object = section.get(path + "." + s);
            if (object instanceof ConfigurationSection)
                loadRecursively(path + "." + s, (ConfigurationSection) object);
            else if (object instanceof Sendable)
                map.put(path + "." + s, Collections.singletonList((Sendable) object));
            else if (object instanceof List && !((List) object).isEmpty())
                map.put(path + "." + s, ((List<?>) object).stream().map(TO_SENDABLE).collect(Collectors.toList()));
            else map.put(path + "." + s, Collections.singletonList(SimpleChatMessage.of(String.valueOf(object))));
        });
    }

}
