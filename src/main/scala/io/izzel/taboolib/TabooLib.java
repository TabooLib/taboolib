package io.izzel.taboolib;

import io.izzel.taboolib.common.plugin.IllegalAccess;
import io.izzel.taboolib.common.plugin.InternalPlugin;
import io.izzel.taboolib.module.config.TConfig;
import io.izzel.taboolib.module.config.TConfigWatcher;
import io.izzel.taboolib.module.db.local.Local;
import io.izzel.taboolib.module.db.local.LocalPlayer;
import io.izzel.taboolib.module.dependency.Dependency;
import io.izzel.taboolib.module.locale.TLocaleLoader;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.IO;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * @Author 坏黑
 * @Since 2019-07-05 10:39
 * <p>
 * 注意与 TabooLib4.x 版本的兼容
 * 可能存在同时运行的情况
 */
@Dependency(
        maven = "org.slf4j:slf4j-api:1.7.25",
        url = "http://repo.ptms.ink/repository/maven-releases/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar;" + "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/org.slf4j-slf4j-api-1.7.25.jar"
)
@Dependency(
        maven = "com.zaxxer:HikariCP:3.4.5",
        url = "http://repo.ptms.ink/repository/maven-releases/public/HikariCP/3.4.5/HikariCP-3.4.5.jar;" + "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/HikariCP-3.4.5.jar"
)
@Dependency(
        maven = "org.scala-lang:scala-library:2.12.8",
        url = "http://repo.ptms.ink/repository/maven-releases/org/scala-lang/scala-library/2.12.8/scala-library-2.12.8.jar;" + "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/scala-library-2.12.8.jar"
)
@Dependency(
        maven = "org.kotlinlang:kotlin-stdlib:1.4.20",
        url = "http://ptms.ink:8081/repository/maven-releases/public/Kotlin/1.4.20/Kotlin-1.4.20-stdlib.jar;" + "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/kotlin-stdlib-1.4.20.jar"
)
@Dependency(
        maven = "org.kotlinlang:kotlin-stdlib-jdk8:1.4.20",
        url = "http://repo.ptms.ink/repository/maven-releases/public/Kotlin/1.4.20/Kotlin-1.4.20-stdlib-jdk8.jar;" + "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/kotlin-stdlib-jdk8-1.4.20.jar"
)
@Dependency(
        maven = "org.kotlinlang:kotlin-stdlib-jdk7:1.4.20",
        url = "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/kotlin-stdlib-jdk7-1.4.20.jar"
)
@Dependency(
        maven = "org.kotlinlang:kotlin-reflect:1.4.20",
        url = "http://repo.ptms.ink/repository/maven-releases/public/Kotlin/1.4.20/Kotlin-1.4.20-reflect.jar;" + "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/kotlin-reflect-1.4.20.jar"
)
@Dependency(
        maven = "com.google.inject:guice:4.2.2",
        url = "http://repo.ptms.ink/repository/maven-releases/com/google/inject/guice/4.2.2/guice-4.2.2.jar;" + "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/guice-4.2.2.jar"
)
@Dependency(
        maven = "com.mongodb:mongodb:3.12.2",
        url = "http://repo.ptms.ink/repository/maven-releases/com/mongodb/MongoDB/3.12.2/MongoDB-3.12.2-all.jar;" + "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/mongodb-driver-3.12.2.jar"
)
public class TabooLib {

    private static TabooLib inst = new TabooLib();
    private static TLogger logger;
    private static TConfig config;

    // 当前运行版本
    private static double version;

    // 内部语言文件
    private final YamlConfiguration internal = new YamlConfiguration();

    @SuppressWarnings("BusyWait")
    public TabooLib() {
        inst = this;
        logger = TLogger.getUnformatted("TabooLib");
        // 配置文件从 config.yml 修改为 settings.yml 防止与老版本插件冲突
        config = TConfig.create(getPlugin(), "settings.yml");
        // 配置更新
        try {
            config.migrate();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // 加载版本号
        try {
            version = NumberConversions.toDouble(IO.readFully(Files.getResource("__resources__/version"), StandardCharsets.UTF_8));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // 加载内部语言文件
        try {
            internal.loadFromString(IO.readFully(Files.getResource("__resources__/lang/internal.yml"), StandardCharsets.UTF_8));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // 加载日志屏蔽
        try {
            IllegalAccess.init();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // 加载 TabooLib 语言文件
        TLocaleLoader.load(getPlugin(), false);
        // 加载 TabooLib
        TabooLibLoader.init();
        // 创建线程检测服务器是否关闭
        Executors.newSingleThreadExecutor().submit(() -> {
            while (NMS.handle().isRunning()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 保存数据
            Local.saveFiles();
            LocalPlayer.saveFiles();
            // 关闭文件监听
            TConfigWatcher.getInst().unregisterAll();
            // 关闭插件
            PluginLoader.stop(getPlugin());
        });
    }

    /**
     * 获取 TaboLib 伪装插件
     *
     * @return {@link InternalPlugin}
     */
    @NotNull
    public static InternalPlugin getPlugin() {
        return InternalPlugin.getPlugin();
    }

    /**
     * 获取 TabooLib 文件实例
     */
    @NotNull
    public static File getTabooLibFile() {
        return new File("libs/TabooLib.jar");
    }

    /**
     * 获取 TabooLib 内部配置文件
     */
    @NotNull
    public YamlConfiguration getInternal() {
        return internal;
    }

    /**
     * 获取 TabooLib 实例
     *
     * @return {@link TabooLib}
     */
    @NotNull
    public static TabooLib getInst() {
        return inst;
    }

    /**
     * 获取 TabooLib 的日志实例
     *
     * @return {@link TLogger}
     */
    @NotNull
    public static TLogger getLogger() {
        return logger;
    }

    /**
     * 获取 TabooLib 配置文件实例
     *
     * @return {@link TConfig}
     */
    @NotNull
    public static TConfig getConfig() {
        return config;
    }

    /**
     * 获取 TabooLib 版本号，如 5.41
     */
    public static double getVersion() {
        return version;
    }
}
