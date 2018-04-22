package com.ilummc.tlib.resources;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.concurrent.ThreadSafe;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.ImmutableList;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.resources.type.TLocaleText;

@ThreadSafe
@SuppressWarnings("rawtypes")
class TLocaleInstance {
	
    TLocaleInstance() {
    }

    void sendTo(String path, CommandSender sender) {
    	try {
    		map.getOrDefault(path, ImmutableList.of(TLocaleSendable.getEmpty(path))).forEach(sendable -> sendable.sendTo(sender));
    	} catch (Exception e) {
    		TLib.getTLib().getLogger().error("语言文件发送失败: " + path);
    		TLib.getTLib().getLogger().error("原因: " + e.getMessage());
    	}
    }

    void sendTo(String path, CommandSender sender, String... args) {
        try {
        	map.getOrDefault(path, ImmutableList.of(TLocaleSendable.getEmpty(path))).forEach(sendable -> sendable.sendTo(sender, args));
        } catch (Exception e) {
        	TLib.getTLib().getLogger().error("语言文件发送失败: " + path);
    		TLib.getTLib().getLogger().error("原因: " + e.getMessage());
        }
    }
    
    String asString(String path) {
    	return map.getOrDefault(path, ImmutableList.of(TLocaleSendable.getEmpty(path))).get(0).asString();
    }

    void load(FileConfiguration configuration) {
        configuration.getKeys(false).forEach(s -> {
            Object object = configuration.get(s);
            if (object instanceof ConfigurationSection) {
                loadRecursively(s, (ConfigurationSection) object);
            }
            else if (object instanceof TLocaleSendable) {
                map.put(s, Collections.singletonList((TLocaleSendable) object));
            }
            else if (object instanceof List && !((List) object).isEmpty()) {
                map.put(s, ((List<?>) object).stream().map(TO_SENDABLE).collect(Collectors.toList()));
            }
            else {
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

    private final Map<String, List<TLocaleSendable>> map = new ConcurrentHashMap<>();

	private void loadRecursively(String path, ConfigurationSection section) {
        section.getKeys(false).forEach(s -> {
            Object object = section.get(path + "." + s);
            if (object instanceof ConfigurationSection) {
                loadRecursively(path + "." + s, (ConfigurationSection) object);
            }
            else if (object instanceof TLocaleSendable) {
                map.put(path + "." + s, Collections.singletonList((TLocaleSendable) object));
            }
            else if (object instanceof List && !((List) object).isEmpty()) {
                map.put(path + "." + s, ((List<?>) object).stream().map(TO_SENDABLE).collect(Collectors.toList()));
            }
            else {
            	map.put(path + "." + s, Collections.singletonList(TLocaleText.of(String.valueOf(object))));
            }
        });
    }

}
