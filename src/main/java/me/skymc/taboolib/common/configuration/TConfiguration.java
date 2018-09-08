package me.skymc.taboolib.common.configuration;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.logger.TLogger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @Author sky
 * @Since 2018-09-08 15:00
 */
public class TConfiguration extends YamlConfiguration {

    private File file;
    private Runnable runnable;

    private TConfiguration(File file) {
        this.file = file;
        reload();
        TLib.getTLib().getConfigWatcher().addSimpleListener(this.file, this::reload);
    }

    /**
     * 释放文件监听
     */
    public void release() {
        TLib.getTLib().getConfigWatcher().removeListener(file);
    }

    /**
     * 重新载入配置
     */
    public void reload() {
        try {
            load(file);
            Optional.ofNullable(runnable).ifPresent(Runnable::run);
        } catch (IOException | InvalidConfigurationException e) {
            TLogger.getGlobalLogger().warn("Cannot load configuration from stream: " + e.toString());
        }
    }

    /**
     * 创建配置文件
     *
     * @param file 文件
     * @return {@link TConfiguration}
     */
    public static TConfiguration create(File file) {
        return new TConfiguration(file);
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
        return create(file);
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public File getFile() {
        return file;
    }

    public TConfiguration listener(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }
}
