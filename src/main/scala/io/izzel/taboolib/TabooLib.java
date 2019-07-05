package io.izzel.taboolib;

import io.izzel.taboolib.module.locale.TLocaleLoader;
import io.izzel.taboolib.module.config.TConfig;
import io.izzel.taboolib.module.config.TConfigWatcher;
import io.izzel.taboolib.module.dependency.Dependency;
import io.izzel.taboolib.module.logger.TLogger;
import io.izzel.taboolib.module.mysql.hikari.HikariHandler;
import io.izzel.taboolib.module.nms.NMSHandler;
import io.izzel.taboolib.origin.database.PlayerDataManager;
import io.izzel.taboolib.origin.database.PluginDataManager;
import io.izzel.taboolib.plugin.InternalPlugin;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.IO;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.NumberConversions;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

/**
 * @Author 坏黑
 * @Since 2019-07-05 10:39
 * <p>
 * 注意与 TabooLib4.x 版本的兼容
 * 可能存在同时运行的情况
 */
@Dependency(type = Dependency.Type.LIBRARY, maven = "org.slf4j:slf4j-api:1.7.25", url = "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/org.slf4j-slf4j-api-1.7.25.jar")
@Dependency(type = Dependency.Type.LIBRARY, maven = "com.zaxxer:HikariCP:3.1.0", url = "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/com.zaxxer-HikariCP-3.1.0.jar")
@Dependency(type = Dependency.Type.LIBRARY, maven = "org.scala-lang:scala-library:2.12.8", url = "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/scala-library-2.12.8.jar")
public class TabooLib {

    private static TabooLib inst = new TabooLib();
    private static TLogger logger;
    private static TConfig config;
    private static double version;

    // 第三方依赖下载位置
    private File libsFolder;
    private File playerDataFolder;
    private File serverDataFolder;

    // 内部语言文件
    private YamlConfiguration internal = new YamlConfiguration();

    public TabooLib() {
        // 创建配置
        inst = this;
        logger = TLogger.getUnformatted("TabooLib");
        config = TConfig.create(getPlugin(), "config.yml");
        libsFolder = Files.folder("plugins/TabooLib/libs");
        playerDataFolder = Files.folder(config.getString("DATAURL.PLAYER-DATA"));
        serverDataFolder = Files.folder(config.getString("DATAURL.SERVER-DATA"));
        // 加载版本号
        try {
            version = NumberConversions.toDouble(IO.readFully(Files.getResource("version"), Charset.forName("utf-8")));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // 加载内部语言文件
        try {
            internal.loadFromString(IO.readFully(Files.getResource("lang/internal.yml"), Charset.forName("utf-8")));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // 加载 TabooLib 语言文件
        TLocaleLoader.load(getPlugin(), false);
        // 加载 TabooLib
        TabooLibLoader.init();
        // 创建 TabooLib 插件数据
        PluginDataManager.addPluginData("TabooLib", null);
        PluginDataManager.addPluginData("TabooLibrary", null);
        // 创建线程检测服务器是否关闭
        Executors.newSingleThreadExecutor().submit(() -> {
            while (NMSHandler.getHandler().isRunning()) {
            }
            // 关闭连接池
            HikariHandler.closeDataSourceForce();
            // 保存数据
            PlayerDataManager.saveAllPlayers(false, true);
            PluginDataManager.saveAllCaches();
            // 插件关闭
            PluginLoader.stop(getPlugin());
            // 清理数据
            if (config.getBoolean("DELETE-DATA")) {
                Files.deepDelete(getPlayerDataFolder());
            }
        });
    }

    public void cancel() {
        TConfigWatcher.getInst().unregisterAll();
    }

    public static InternalPlugin getPlugin() {
        return InternalPlugin.getPlugin();
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static TabooLib getInst() {
        return inst;
    }

    public static TLogger getLogger() {
        return logger;
    }

    public static TConfig getConfig() {
        return config;
    }

    public static double getVersion() {
        return version;
    }

    public File getLibsFolder() {
        return libsFolder;
    }

    public File getPlayerDataFolder() {
        return playerDataFolder;
    }

    public File getServerDataFolder() {
        return serverDataFolder;
    }

    public YamlConfiguration getInternal() {
        return internal;
    }
}
