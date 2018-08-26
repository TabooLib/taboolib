package me.skymc.taboolib;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.util.IO;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.database.GlobalDataManager;
import me.skymc.taboolib.database.PlayerDataManager;
import me.skymc.taboolib.economy.EcoUtils;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.inventory.speciaitem.SpecialItem;
import me.skymc.taboolib.itagapi.TagDataHandler;
import me.skymc.taboolib.javascript.ScriptHandler;
import me.skymc.taboolib.listener.TListenerHandler;
import me.skymc.taboolib.mysql.hikari.HikariHandler;
import me.skymc.taboolib.mysql.protect.MySQLConnection;
import me.skymc.taboolib.nms.item.DabItemUtils;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.permission.PermissionUtils;
import me.skymc.taboolib.playerdata.DataUtils;
import me.skymc.taboolib.skript.SkriptHandler;
import me.skymc.taboolib.socket.TabooLibClient;
import me.skymc.taboolib.string.language2.Language2;
import me.skymc.taboolib.support.SupportPlaceholder;
import me.skymc.taboolib.timecycle.TimeCycleManager;
import me.skymc.taboolib.translateuuid.TranslateUUID;
import me.skymc.taboolib.update.UpdateTask;
import me.skymc.tlm.TLM;
import me.skymc.tlm.module.TabooLibraryModule;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
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
    private static Economy economy;
    private static File playerDataFolder;
    private static File serverDataFolder;
    private static StorageType storageType = StorageType.LOCAL;
    private static Language2 exampleLanguage2;
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
        // 载入牛逼东西
        TLib.init();
        TLib.injectPluginManager();
        // 载入插件设置
        TabooLibLoader.setup();
        // 载入大饼
        TLib.initPost();
        // 载入连接池
        HikariHandler.init();
    }

    @Override
    public void onEnable() {
        // 注册插件配置
        TabooLibLoader.register();
        // 载入经济
        EcoUtils.setupEconomy();
        // 载入权限
        PermissionUtils.loadRegisteredServiceProvider();
        // 物品名称
        ItemUtils.LoadLib();
        // 低层工具
        DabItemUtils.getInstance();
        // 载入周期管理器
        TimeCycleManager.load();
        // 启动脚本
        ScriptHandler.inst();
        // 注册脚本
        SkriptHandler.register();
        // 注册头衔
        TagDataHandler.init(this);
        // 载入语言文件
        exampleLanguage2 = new Language2("Language2", this);
        // 启动数据库储存方法
        if (getStorageType() == StorageType.SQL) {
            GlobalDataManager.SQLMethod.startSQLMethod();
        }
        // 载入完成
        TLocale.Logger.info("NOTIFY.SUCCESS-LOADED", getDescription().getAuthors().toString(), getDescription().getVersion(), String.valueOf(TabooLib.getVersion()));
        // 文件保存
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, DataUtils::saveAllCaches, 20, 20 * 120);
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
                // 载入 PlaceholderAPI 扩展
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    new SupportPlaceholder(getInst(), "taboolib").hook();
                }
                // 载入 TLM 接口
                TLM.getInst();
                // 载入 SpecialItem 接口
                SpecialItem.getInst().loadItems();
                // 载入 TranslateUUID 工具
                TranslateUUID.init();
                // 面子工程
                InputStream inputStream = FileUtils.getResource("motd.txt");
                try {
                    String text = new String(IO.readFully(inputStream), Charset.forName("utf-8"));
                    if (text != null) {
                        Arrays.stream(text.split("\n")).forEach(line -> Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(line, getDescription().getVersion())));
                    }
                } catch (IOException ignored) {
                }
                // 本地通讯网络
                TabooLibClient.init();
            }
        }.runTask(this);
        // 更新检测
        new UpdateTask();
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
        // 保存数据
        Bukkit.getOnlinePlayers().forEach(x -> DataUtils.saveOnline(x.getName()));
        // 结束线程
        Bukkit.getScheduler().cancelTasks(this);
        // 保存插件数据
        DataUtils.saveAllCaches();
        // 保存玩家数据
        PlayerDataManager.saveAllPlayers(false, true);
        // 注销 SpecialItem 接口
        SpecialItem.getInst().unloadItems();
        // 注销 TLM 接口
        TabooLibraryModule.getInst().unloadModules();
        // 注销 TranslateUUID 接口
        TranslateUUID.cancel();
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

    public static net.milkbowl.vault.economy.Economy getEconomy() {
        return economy;
    }

    public static void setEconomy(Economy economy) {
        Main.economy = economy;
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

    public static MySQLConnection getConnection() {
        return null;
    }

    public static Language2 getExampleLanguage2() {
        return exampleLanguage2;
    }

    public static boolean isStarted() {
        return started;
    }

    public static Random getRandom() {
        return NumberUtils.getRandom();
    }

    public static String getTablePrefix() {
        return inst.getConfig().getString("MYSQL.PREFIX");
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
