package io.izzel.taboolib.module.db.local;

import io.izzel.taboolib.module.lite.SimpleReflection;
import io.izzel.taboolib.util.Files;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * @Author sky, yumc
 * @Since 2020-02-28 11:14
 */
public class SecuredFile extends YamlConfiguration {

    public SecuredFile() {
        SimpleReflection.setFieldValue(MemorySection.class, this, "map", new ConcurrentHashMap<>(map), true);
    }

    @Override
    public ConfigurationSection createSection(String path) {
        ConfigurationSection section = super.createSection(path);
        if (section instanceof MemorySection) {
            SimpleReflection.setFieldValue(MemorySection.class, section, "map", new ConcurrentHashMap<>(), true);
        }
        return section;
    }

    /**
     * 如果文件读取失败则创建备份
     * 以防出现不可逆的损伤
     */
    @Override
    public void load(File file) throws InvalidConfigurationException {
        String content = Files.readFromFile(file);
        try {
            loadFromString(content);
        } catch (InvalidConfigurationException t) {
            if (!file.getName().endsWith(".bak")) {
                Files.copy(file, new File(file.getParent(), file.getName() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis()) + ".bak"));
            }
            throw t;
        }
    }

    /**
     * 如果文本读取失败则打印到日志
     * 以防出现不可逆的损伤
     */
    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        try {
            super.loadFromString(contents);
        } catch (InvalidConfigurationException t) {
            System.out.println("Source: \n" + contents);
            throw t;
        }
    }

    public static String dump(Object data) {
        Yaml yaml = (Yaml) SimpleReflection.getFieldValueChecked(YamlConfiguration.class, new YamlConfiguration(), "yaml", true);
        try {
            return yaml.dump(data);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return "";
    }

    public static SecuredFile loadConfiguration(String contents) {
        SecuredFile config = new SecuredFile();
        try {
            config.loadFromString(contents);
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load source", t);
        }
        return config;
    }

    public static SecuredFile loadConfiguration(File file) {
        SecuredFile config = new SecuredFile();
        try {
            config.load(file);
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, t);
        }
        return config;
    }
}
