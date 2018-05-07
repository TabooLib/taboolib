package com.ilummc.tlib.inject;

import com.google.common.io.Files;
import com.ilummc.tlib.annotations.TConfig;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.fileutils.ConfigUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;

public class TConfigInjector {

    public static void fixUnicode(YamlConfiguration configuration) {
        try {
            Field field = YamlConfiguration.class.getDeclaredField("yamlOptions");
            field.setAccessible(true);
            field.set(configuration, NoUnicodeDumperOption.INSTANCE);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static final class NoUnicodeDumperOption extends DumperOptions {

        private static final NoUnicodeDumperOption INSTANCE = new NoUnicodeDumperOption();

        @Override
        public void setAllowUnicode(boolean allowUnicode) {
            super.setAllowUnicode(false);
        }

        @Override
        public boolean isAllowUnicode() {
            return false;
        }

        @Override
        public void setLineBreak(LineBreak lineBreak) {
            super.setLineBreak(LineBreak.getPlatformLineBreak());
        }
    }

    public static Object loadConfig(Plugin plugin, Class<?> clazz) {
        try {
            TConfig config = clazz.getAnnotation(TConfig.class);
            Validate.notNull(config);
            File file = new File(plugin.getDataFolder(), config.name());
            if (!file.exists()) if (config.fromJar()) plugin.saveResource(config.name(), true);
            else saveConfig(plugin, clazz.newInstance());
            Object obj = unserialize(plugin, clazz);
            if (!config.readOnly()) saveConfig(plugin, obj);
            return obj;
        } catch (NullPointerException e) {
            TLocale.Logger.warn("CONFIG.LOAD-FAIL-NO-ANNOTATION", plugin.toString(), clazz.getSimpleName());
        } catch (Exception e) {
            TLocale.Logger.warn("CONFIG.LOAD-FAIL", plugin.toString(), clazz.getSimpleName());
        }
        return null;
    }

    public static void reloadConfig(Plugin plugin, Object object) {
        try {
            TConfig config = object.getClass().getAnnotation(TConfig.class);
            Validate.notNull(config);
            File file = new File(plugin.getDataFolder(), config.name());
            Map<String, Object> map = ConfigUtils.confToMap(ConfigUtils.loadYaml(plugin, file));
            Object obj = ConfigUtils.mapToObj(map, object);
            if (!config.readOnly()) saveConfig(plugin, obj);
        } catch (NullPointerException e) {
            TLocale.Logger.warn("CONFIG.LOAD-FAIL-NO-ANNOTATION", plugin.toString(), object.getClass().getSimpleName());
        } catch (Exception e) {
            TLocale.Logger.warn("CONFIG.LOAD-FAIL", plugin.toString(), object.getClass().getSimpleName());
        }
    }

    public static Object unserialize(Plugin plugin, Class<?> clazz) {
        try {
            TConfig config = clazz.getAnnotation(TConfig.class);
            Validate.notNull(config);
            return ConfigUtils.confToObj(
                    ConfigUtils.mapToConf(
                            ConfigUtils.yamlToMap(
                                    Files.toString(new File(plugin.getDataFolder(), config.name()), Charset.forName(config.charset())))), clazz);
        } catch (NullPointerException e) {
            TLocale.Logger.warn("CONFIG.LOAD-FAIL-NO-FILE", plugin.toString(), clazz.getSimpleName());
            return null;
        } catch (Exception e) {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e1) {
                TLocale.Logger.warn("CONFIG.LOAD-FAIL", plugin.toString(), clazz.getSimpleName());
                return null;
            }
        }
    }

    public static Map<String, Object> serialize(Plugin plugin, Object object) {
        try {
            TConfig config = object.getClass().getAnnotation(TConfig.class);
            Validate.notNull(config);
            return ConfigUtils.objToConf(object).getValues(false);
        } catch (NullPointerException e) {
            TLocale.Logger.warn("CONFIG.SAVE-FAIL-NO-ANNOTATION", plugin.toString(), object.getClass().getSimpleName());
        } catch (Exception e) {
            TLocale.Logger.warn("CONFIG.SAVE-FAIL", plugin.toString(), object.getClass().getSimpleName());
        }
        return null;
    }

    public static void saveConfig(Plugin plugin, Object object) throws IOException, NullPointerException {
        TConfig config = object.getClass().getAnnotation(TConfig.class);
        Validate.notNull(config);
        Object obj = serialize(plugin, object);
        Validate.notNull(obj);
        File target = new File(plugin.getDataFolder(), config.name());
        if (!target.exists()) target.createNewFile();
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(false);
        Yaml yaml = new Yaml(options);
        String str = yaml.dump(obj);
        byte[] arr = str.getBytes(config.charset());
        Files.write(arr, target);
    }

}
