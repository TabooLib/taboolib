package taboolib.platform;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.PrimitiveIO;
import taboolib.common.PrimitiveSettings;
import taboolib.common.TabooLib;
import taboolib.common.classloader.IsolatedClassLoader;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static taboolib.common.PrimitiveIO.t;

/**
 * TabooLib
 * taboolib.platform.HytalePlugin
 */
@SuppressWarnings({"Convert2Lambda", "DuplicatedCode", "CallToPrintStackTrace"})
@PlatformSide(Platform.HYTALE)
public class HytalePlugin extends JavaPlugin {

    @Nullable
    private static Plugin pluginInstance;
    private static HytalePlugin instance;

    static {
        PrimitiveIO.debug("Hytale 插件初始化完成，用时 {0} 毫秒。", TabooLib.execution(() -> {
            try {
                // 初始化 IsolatedClassLoader
                IsolatedClassLoader.init(HytalePlugin.class);
            } catch (Throwable ex) {
                if (PrimitiveSettings.IS_DISABLE_WHEN_PRIMITIVE_LOADER_ERROR) {
                    TabooLib.setStopped(true);
                    PrimitiveIO.error(
                            t(
                                    "无法初始化原始加载器，为避免数据丢失，服务器将会被强制关闭！",
                                    "Failed to initialize primitive loader. To avoid data loss, the server will be forced to shut down!"
                            )
                    );
                    ex.printStackTrace();
                    try {
                        Thread.sleep(3000);
                    } catch (Throwable ignored) {
                    }
                    Runtime.getRuntime().halt(-1);
                } else {
                    TabooLib.setStopped(true);
                    PrimitiveIO.error(
                            t(
                                    "无法初始化原始加载器，插件 \"{0}\" 将被禁用！",
                                    "Failed to initialize primitive loader, the plugin \"{0}\" will be disabled!"
                            ),
                            PrimitiveIO.getRunningFileName()
                    );
                    throw ex;
                }
            }
            // 生命周期任务
            TabooLib.lifeCycle(LifeCycle.CONST);
            // 检索 TabooLib Plugin 实现
            pluginInstance = Plugin.getInstance();
        }));
    }

    public HytalePlugin(@NotNull JavaPluginInit init) {
        super(init);
        instance = this;
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.INIT);
    }

    @Override
    protected void setup() {
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.LOAD);
        // 调用 Plugin 实现的 onLoad() 方法
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onLoad();
        }
    }

    @Override
    protected void start() {
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.ENABLE);
        // 判断插件是否关闭
        if (!TabooLib.isStopped()) {
            // 调用 Plugin 实现的 onEnable() 方法
            if (pluginInstance != null) {
                pluginInstance.onEnable();
            }
        }
        // 再次判断插件是否关闭
        // 因为插件可能在 onEnable() 下关闭
        if (!TabooLib.isStopped()) {
            // 创建调度器，执行 onActive() 方法
            HytaleServer.SCHEDULED_EXECUTOR.schedule(new Runnable() {
                @Override
                public void run() {
                    // 生命周期任务
                    TabooLib.lifeCycle(LifeCycle.ACTIVE);
                    // 调用 Plugin 实现的 onActive() 方法
                    if (pluginInstance != null) {
                        pluginInstance.onActive();
                    }
                }
            }, 0, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void shutdown() {
        // 在插件未关闭的前提下，执行 onDisable() 方法
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onDisable();
        }
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.DISABLE);
    }

    /**
     * 获取插件文件
     */
    @NotNull
    public File getPluginFile() {
        return getFile().toFile();
    }

    /**
     * 获取数据目录
     */
    @NotNull
    public File getPluginDataFolder() {
        return getDataDirectory().toFile();
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return pluginInstance;
    }

    @NotNull
    public static HytalePlugin getInstance() {
        return instance;
    }
}
