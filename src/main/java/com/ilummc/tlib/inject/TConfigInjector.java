package com.ilummc.tlib.inject;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.annotations.Config;
import com.ilummc.tlib.bean.Property;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class TConfigInjector {

    public static Object loadConfig(Plugin plugin, Class<?> clazz) {
        try {
            Config config = clazz.getAnnotation(Config.class);
            Validate.notNull(config);
            File file = new File(plugin.getDataFolder(), config.name());
            if (!file.exists()) if (config.fromJar()) plugin.saveResource(config.name(), true);
            else saveConfig(plugin, clazz);
            return unserialize(plugin, clazz);
        } catch (NullPointerException e) {
            TLib.getTLib().getLogger().warn("插件 " + plugin + " 的配置类 " + clazz.getSimpleName() + " 加载失败：没有 @Config 注解");
        } catch (Exception e) {
            TLib.getTLib().getLogger().warn("插件 " + plugin + " 的配置类 " + clazz.getSimpleName() + " 加载失败");
        }
        return null;
    }

    public static Object unserialize(Plugin plugin, Class<?> clazz) {
        try {
            Config config = clazz.getAnnotation(Config.class);
            Validate.notNull(config);
            return new GsonBuilder().disableHtmlEscaping().excludeFieldsWithModifiers(config.excludeModifiers())
                    .create().fromJson(new Gson().toJson(new Yaml()
                            .dump(Files.toString(new File(plugin.getDataFolder(), config.name()), Charset.forName(config.charset())))), clazz);
        } catch (NullPointerException e) {
            TLib.getTLib().getLogger().warn("插件 " + plugin + " 的配置类 " + clazz.getSimpleName() + " 加载失败：没有 @Config 注解");
            return null;
        } catch (Exception e) {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e1) {
                TLib.getTLib().getLogger().warn("插件 " + plugin + " 的配置类 " + clazz.getSimpleName() + " 加载失败");
                return null;
            }
        }
    }

    public static Map<String, Object> serialize(Plugin plugin, Class<?> clazz) {
        try {
            Constructor constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            Config config = clazz.getAnnotation(Config.class);
            Validate.notNull(config);
            return new Serializer(new LinkedHashMap<>(), constructor.newInstance(), config.excludeModifiers()).get();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            TLib.getTLib().getLogger().warn("插件 " + plugin + " 的配置类 " + clazz.getSimpleName() + " 序列化失败：没有无参构造方法");
        } catch (NullPointerException e) {
            TLib.getTLib().getLogger().warn("插件 " + plugin + " 的配置类 " + clazz.getSimpleName() + " 序列化失败：没有 @Config 注解");
        } catch (Exception e) {
            TLib.getTLib().getLogger().warn("插件 " + plugin + " 的配置类 " + clazz.getSimpleName() + " 序列化失败");
        }
        return null;
    }

    public static void saveConfig(Plugin plugin, Class<?> clazz) throws IOException, NullPointerException {
        Object obj = serialize(plugin, clazz);
        Validate.notNull(obj);
        Config config = clazz.getAnnotation(Config.class);
        Validate.notNull(config);
        File target = new File(plugin.getDataFolder(), config.name());
        if (!target.exists()) target.createNewFile();
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        String str = yaml.dump(obj);
        byte[] arr = str.getBytes(config.charset());
        Files.write(arr, target);
    }

    private static final List<Class> primitiveType = Lists.newArrayList(Integer.class,
            Double.class, Float.class, Boolean.class, Short.class, Byte.class, Character.class, Long.class, String.class);

    private static class Serializer {

        private HashMap<String, Object> map;
        private Object o;
        private int modifiers;

        private Serializer(HashMap<String, Object> map, Object o, int modifiers) {
            this.map = map;
            this.o = o;
            this.modifiers = modifiers;
        }

        private HashMap<String, Object> get() {
            for (Field field : o.getClass().getDeclaredFields()) {
                if ((field.getModifiers() & modifiers) == 0 && !field.isSynthetic())
                    try {
                        SerializedName node = field.getAnnotation(SerializedName.class);
                        if (!field.isAccessible()) field.setAccessible(true);
                        Object obj = field.get(o);
                        map.put(node == null ? field.getName() : node.value(), serialize(obj));
                    } catch (Exception ignored) {
                    }
            }
            return map;
        }

        @SuppressWarnings({"unchecked"})
        private Object serialize(Object o) {
            try {
                if (o.getClass().isPrimitive() || primitiveType.contains(o.getClass())) {
                    return o;
                } else if (o.getClass().isArray()) {
                    List list = new ArrayList();
                    int len = (int) o.getClass().getField("length").get(o);
                    for (int i = 0; i < len; i++) {
                        list.add(serialize(Array.get(o, i)));
                    }
                    return list;
                } else if (o instanceof Collection) {
                    return ((Collection) o).stream().map(this::serialize).collect(Collectors.toList());
                } else if (o instanceof Map) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    ((Map) o).forEach((o1, o2) -> map.put((String) o1, serialize(o2)));
                    return map;
                } else if (o instanceof ConfigurationSerializable) {
                    Map map = new LinkedHashMap();
                    map.put("==", o.getClass().getName());
                    map.putAll(((ConfigurationSerializable) o).serialize());
                    return map;
                } else if (o instanceof Property) {
                    return serialize(((Property) o).get());
                } else {
                    return new Serializer(new HashMap<>(), o, modifiers).get();
                }
            } catch (Exception ignored) {
                return null;
            }
        }

    }

}
