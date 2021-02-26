package io.izzel.taboolib.common.plugin;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.util.Files;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author 坏黑
 * @since 2019-07-05 14:02
 */
public class InternalPlugin implements Plugin {

    private static final InternalPlugin plugin;

    public static InternalPlugin getPlugin() {
        return plugin;
    }

    static {
        plugin = new InternalPlugin();
    }

    @Override
    public File getDataFolder() {
        return new File("plugins/TabooLib");
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return new PluginDescriptionFile("TabooLib", String.valueOf(TabooLib.getVersion()), "io.izzel.taboolib.common.plugin.InternalJavaPlugin");
    }

    @Override
    public FileConfiguration getConfig() {
        return new YamlConfiguration();
    }

    @Override
    public InputStream getResource(String s) {
        return Files.getTabooLibResource(s);
    }

    @Override
    public void saveConfig() {
    }

    @Override
    public void saveDefaultConfig() {
    }

    @Override
    public void saveResource(String s, boolean b) {
        File file = new File(getDataFolder(), s);
        if (!file.exists() || b) {
            Files.toFile(getResource(s), Files.file(file));
        }
    }

    @Override
    public void reloadConfig() {
    }

    @Override
    public PluginLoader getPluginLoader() {
        return InternalPluginLoader.getLoader();
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public boolean isNaggable() {
        return false;
    }

    @Override
    public void setNaggable(boolean b) {
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String s, String s1) {
        return null;
    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public String getName() {
        return "TabooLib";
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }

    public void runTask(Runnable runnable) {
        Bukkit.getScheduler().runTask(this, () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void runTaskAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void runTask(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, delay);
    }

    public void runTaskAsync(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, delay);
    }

    public void runTask(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, delay, period);
    }

    public void runTaskAsync(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, delay, period);
    }
}
