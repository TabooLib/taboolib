package me.skymc.taboolib.common.configuration;

import com.google.common.collect.Maps;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.logger.TLogger;
import com.ilummc.tlib.util.Ref;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author sky
 * @Since 2018-09-08 15:00
 */
public class TConfiguration extends YamlConfiguration {

    private static Map<String, List<File>> files = Maps.newHashMap();
    private File file;
    private Runnable runnable;

    private TConfiguration(File file, Plugin plugin) {
        files.computeIfAbsent(plugin.getName(), name -> new ArrayList<>()).add(file);
        this.file = file;
        reload();
        TLib.getTLib().getConfigWatcher().addSimpleListener(this.file, this::reload);
        TabooLib.debug("Loaded TConfiguration \"" + file.getName() + "\" from Plugin \"" + plugin.getName() + "\"");
    }

    /**
     * 创建配置文件
     *
     * @param file 文件
     * @return {@link TConfiguration}
     */
    public static TConfiguration create(File file) {
        return new TConfiguration(file, Ref.getCallerPlugin(Ref.getCallerClass(3).orElse(Main.class)));
    }

    /**
     * 创建配置文件
     *
     * @param file   文件
     * @param plugin 插件
     * @return {@link TConfiguration}
     */
    public static TConfiguration create(File file, Plugin plugin) {
        return new TConfiguration(file, plugin);
    }

    /**
     * 从插件里释放文件并创建
     *
     * @param plugin 插件
     * @param path   目录
     * @return {@link TConfiguration}
     */
    public static TConfiguration createInResource(Plugin plugin, String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.saveResource(path, true);
        }
        return create(file, plugin);
    }

    public static Map<String, List<File>> getFiles() {
        return files;
    }

    public void release() {
        TLib.getTLib().getConfigWatcher().removeListener(file);
    }

    public void reload() {
        try {
            load(file);
            runListener();
        } catch (IOException | InvalidConfigurationException e) {
            TLogger.getGlobalLogger().warn("Cannot load configuration from stream: " + e.toString());
        }
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public void runListener() {
        try {
            Optional.ofNullable(runnable).ifPresent(Runnable::run);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return file;
    }

    public TConfiguration listener(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }
}
