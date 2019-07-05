package me.skymc.taboolib;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.filter.TLoggerFilter;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.database.GlobalDataManager;
import me.skymc.taboolib.database.PlayerDataManager;
import me.skymc.taboolib.economy.EcoUtils;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.listener.TListenerHandler;
import me.skymc.taboolib.mysql.hikari.HikariHandler;
import me.skymc.taboolib.mysql.protect.MySQLConnection;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.playerdata.DataUtils;
import me.skymc.taboolib.socket.TabooLibClient;
import me.skymc.taboolib.socket.TabooLibServer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Random;

/**
 * @author sky
 */
public class Main extends JavaPlugin {

    public Main() {
        inst = this;
    }

    public enum StorageType {
        LOCAL, SQL
    }

    private static Plugin inst;
    private static File playerDataFolder;
    private static File serverDataFolder;
    private static StorageType storageType = StorageType.LOCAL;
    private static boolean disable = false;
    private static boolean started = false;
    private static boolean isInternetOnline = false;
    private FileConfiguration config = null;

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void saveDefaultConfig() {
        reloadConfig();
    }

    @Override
    public void reloadConfig() {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveResource("config.yml", true);
        }
        config = ConfigUtils.load(this, file);
    }

    @Override
    public void onLoad() {
        disable = false;
        // 载入配置文件
        saveDefaultConfig();
        // 载入日志过滤
        TLoggerFilter.preInit();
        // 载入扩展
        TabooLibLoader.setupAddons();
        // 载入牛逼东西
        TLib.init();
        TLib.injectPluginManager();
        // 载入插件设置
        TabooLibLoader.setup();
        // 载入大饼
        TLib.initPost();
    }

    @Override
    public void onEnable() {
        // 载入日志过滤
        TLoggerFilter.postInit();
        // 注册插件配置
        TabooLibLoader.register();
        // 启动数据库储存方法
        if (getStorageType() == StorageType.SQL) {
            GlobalDataManager.SQLMethod.startSQLMethod();
        }
        // 载入完成
        TLocale.Logger.info("NOTIFY.SUCCESS-LOADED", getDescription().getAuthors().toString(), getDescription().getVersion(), String.valueOf(TabooLib.getVersion()));
        // 文件保存
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> DataUtils.saveAllCaches(), 20, 20 * 120);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> PlayerDataManager.saveAllCaches(true, false), 20, 20 * 60);
        // 文件监控
        TLib.getTLib().getConfigWatcher().addSimpleListener(new File(getDataFolder(), "config.yml"), () -> {
            reloadConfig();
            TLocale.Logger.info("CONFIG.RELOAD-SUCCESS", inst.getName(), "config.yml");
        });
        // 插件联动
        new BukkitRunnable() {

            @Override
            public void run() {
                // 本地通讯网络终端
                if (getConfig().getBoolean("SERVER")) {
                    TabooLibServer.main(new String[0]);
                }
                // 本地通讯网络
                TabooLibClient.init();
            }
        }.runTask(this);
        // 启动
        started = true;
    }

    @Override
    public void onDisable() {
        disable = true;
        // 如果插件尚未启动完成
        if (!started) {
            TLocale.Logger.error("NOTIFY.FAIL-DISABLE");
            return;
        }
        // 注销插件
        TabooLibLoader.unregister();
        // 保存数据
        Bukkit.getOnlinePlayers().forEach(x -> DataUtils.saveOnline(x.getName()));
        // 结束线程
        Bukkit.getScheduler().cancelTasks(this);
        // 保存插件数据
        DataUtils.saveAllCaches();
        // 保存玩家数据
        PlayerDataManager.saveAllPlayers(false, true);
        // 注销连接池
        HikariHandler.closeDataSourceForce();
        // 注销监听器
        TListenerHandler.cancelListeners();
        // 结束数据库储存方法
        if (getStorageType() == StorageType.SQL) {
            GlobalDataManager.SQLMethod.cancelSQLMethod();
        }
        // 清理数据
        if (getStorageType() == StorageType.LOCAL && getConfig().getBoolean("DELETE-DATA")) {
            getPlayerDataFolder().delete();
        }
        // 清理数据
        if (getStorageType() == StorageType.SQL && getConfig().getBoolean("DELETE-VARIABLE")) {
            GlobalDataManager.clearInvalidVariables();
        }
        // 提示信息
        TLocale.Logger.info("NOTIFY.SUCCESS-DISABLE");
        // 卸载牛逼玩意儿
        TLib.unload();
        // 关闭服务器
        Bukkit.shutdown();
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static Plugin getInst() {
        return inst;
    }

    public static String getPrefix() {
        return "§8[§3§lTabooLib§8] §7";
    }

    public static File getPlayerDataFolder() {
        return playerDataFolder;
    }

    public static File getServerDataFolder() {
        return serverDataFolder;
    }

    public static StorageType getStorageType() {
        return storageType;
    }

    public static boolean isDisable() {
        return disable;
    }

    public static boolean isStarted() {
        return started;
    }

    public static boolean isInternetOnline() {
        return isInternetOnline;
    }

    public static boolean isOfflineVersion() {
        return inst.getResource("libs") != null;
    }

    public static boolean isLibrariesExists() {
        return TLib.getTLib().getLibsFolder().listFiles().length > 0;
    }

    @Deprecated
    public static Random getRandom() {
        return NumberUtils.getRandom();
    }

    @Deprecated
    public static String getTablePrefix() {
        return inst.getConfig().getString("MYSQL.PREFIX");
    }

    @Deprecated
    public static MySQLConnection getConnection() {
        return null;
    }

    @Deprecated
    public static net.milkbowl.vault.economy.Economy getEconomy() {
        return EcoUtils.getEconomy();
    }

    // *********************************
    //
    //        Private Setter
    //
    // *********************************

    static void setIsInternetOnline(boolean isInternetOnline) {
        Main.isInternetOnline = isInternetOnline;
    }

    static void setPlayerDataFolder(File playerDataFolder) {
        Main.playerDataFolder = playerDataFolder;
    }

    static void setServerDataFolder(File serverDataFolder) {
        Main.serverDataFolder = serverDataFolder;
    }

    static void setStorageType(StorageType storageType) {
        Main.storageType = storageType;
    }
}
