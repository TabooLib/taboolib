package me.skymc.taboolib.fileutils;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.bean.Property;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.util.Ref;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigUtils {

    private static final Yaml YAML;

    static {
        DumperOptions options = new DumperOptions();
        options.setAllowUnicode(false);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        YAML = new Yaml(options);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> yamlToMap(String yamlText) {
        return YAML.loadAs(yamlText, LinkedHashMap.class);
    }

    public static MemoryConfiguration objToConf(Object object) {
        return mapToConf(objToMap(object));
    }

    public static MemoryConfiguration objToConf(Object object, int excludedModifiers) {
        return mapToConf(objToMap(object, excludedModifiers));
    }

    public static <T> Object confToObj(MemoryConfiguration configuration, Class<T> clazz) {
        try {
            return mapToObj(configuration.getValues(false), clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    public static <T> Object confToObj(MemoryConfiguration configuration, T obj) {
        return mapToObj(configuration.getValues(false), obj);
    }

    public static String mapToYaml(Map<String, Object> map) {
        String dump = YAML.dump(map);
        if (dump.equals("{}\n")) {
            dump = "";
        }
        return dump;
    }

    public static MemoryConfiguration mapToConf(Map<String, Object> map) {
        MemoryConfiguration configuration = new MemoryConfiguration();
        convertMapsToSections(map, configuration);
        return configuration;
    }

    public static Map<String, Object> confToMap(MemoryConfiguration configuration) {
        return configuration.getValues(false);
    }

    /**
     * 将会在该类无默认构造方法时返回 null
     */
    public static <T> T mapToObj(Map<String, Object> map, Class<T> clazz) {
        try {
            return mapToObj(map, clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T mapToObj(Map<String, Object> map, T obj) {
        Class<?> clazz = obj.getClass();
        map.forEach((string, value) -> Ref.getFieldBySerializedName(clazz, string).ifPresent(field -> {
            if (!field.isAccessible())
                field.setAccessible(true);
            try {
                if (Property.class.isAssignableFrom(field.getType())) {
                    Property<Object> property = (Property) field.get(obj);
                    if (property != null) property.set(value);
                    else field.set(obj, Property.of(value));
                } else {
                    field.set(obj, value);
                }
            } catch (IllegalAccessException ignored) {
            }
        }));
        return obj;
    }

    public static Map<String, Object> objToMap(Object object) {
        return objToMap(object, Modifier.TRANSIENT & Modifier.STATIC & Ref.ACC_SYNTHETIC);
    }

    public static Map<String, Object> objToMap(Object object, int excludedModifiers) {
        Map<String, Object> map = Maps.newHashMap();
        for (Field field : Ref.getDeclaredFields(object.getClass(), excludedModifiers, false)) {
            try {
                if (!field.isAccessible()) field.setAccessible(true);
                Object obj = field.get(object);
                if (obj instanceof Property) obj = ((Property) obj).get();
                map.put(Ref.getSerializedName(field), obj);
            } catch (IllegalAccessException ignored) {
            }
        }
        return map;
    }

    private static void convertMapsToSections(Map<?, ?> input, ConfigurationSection section) {
        for (Object o : input.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry) o;
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            if (value instanceof Map) {
                convertMapsToSections((Map) value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }
    }

    public static FileConfiguration decodeYAML(String args) {
        return YamlConfiguration.loadConfiguration(new StringReader(Base64Coder.decodeString(args)));
    }

    public static String encodeYAML(FileConfiguration file) {
        return Base64Coder.encodeLines(file.saveToString().getBytes()).replaceAll("\\s+", "");
    }

    /**
     * 以 UTF-8 的格式释放配置文件并载入
     * <p>
     * 录入时间：2018年2月10日21:28:30
     * 录入版本：3.49
     *
     * @param plugin
     * @return
     */
    public static FileConfiguration saveDefaultConfig(Plugin plugin, String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, true);
        }
        return load(plugin, file);
    }

    /**
     * 以 UTF-8 的格式载入配置文件
     *
     * @return
     */
    public static FileConfiguration load(Plugin plugin, File file) {
        return loadYaml(plugin, file);
    }

    public static YamlConfiguration loadYaml(Plugin plugin, File file) {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            String yaml = Files.toString(file, Charset.forName("utf-8"));
            configuration.loadFromString(yaml);
            return configuration;
        } catch (Exception e) {
            TLocale.Logger.error("FILE-UTILS.FALL-LOAD-CONFIGURATION", plugin.getName(), file.getName());
        }
        return configuration;
    }


    @Deprecated
    public static FileConfiguration load(Plugin plugin, String file) {
        return load(plugin, FileUtils.file(file));
    }
}
