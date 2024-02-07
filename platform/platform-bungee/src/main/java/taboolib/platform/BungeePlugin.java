package taboolib.platform;

import net.md_5.bungee.BungeeCord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.PrimitiveIO;
import taboolib.common.PrimitiveSettings;
import taboolib.common.TabooLib;
import taboolib.common.classloader.IsolatedClassLoader;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformImplementationKt;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * TabooLib
 * taboolib.platform.BungeePlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"Convert2Lambda", "DuplicatedCode", "CallToPrintStackTrace"})
@PlatformSide(Platform.BUNGEE)
public class BungeePlugin extends net.md_5.bungee.api.plugin.Plugin {

    @Nullable
    private static final Plugin pluginInstance;
    private static BungeePlugin instance;

    static {
        long time = System.currentTimeMillis();
        // 初始化 IsolatedClassLoader
        try {
            IsolatedClassLoader.init(BungeePlugin.class);
        } catch (Throwable ex) {
            // 提示信息
            PrimitiveIO.error("[TabooLib] Failed to initialize primitive loader, the plugin \"%s\" will be disabled!", PrimitiveIO.getRunningFileName());
            // 重抛错误
            throw ex;
        }
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.CONST);
        // 检索 TabooLib Plugin 实现
        pluginInstance = Plugin.findImpl();
        // 调试模式显示加载耗时
        if (PrimitiveSettings.IS_DEV_MODE) {
            PrimitiveIO.println("[TabooLib] \"%s\" Initialization completed. (%sms)", PrimitiveIO.getRunningFileName(), System.currentTimeMillis() - time);
        }
    }

    public BungeePlugin() {
        instance = this;
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.INIT);
    }

    @Override
    public void onLoad() {
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.LOAD);
        // 调用 Plugin 实现的 onLoad() 方法
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onLoad();
        }
    }

    @Override
    public void onEnable() {
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.ENABLE);
        // 判断插件是否关闭
        if (!TabooLib.isStopped()) {
            // 调用 Plugin 实现的 onEnable() 方法
            if (pluginInstance != null) {
                pluginInstance.onEnable();
            }
            // 启动调度器
            try {
                Object o = TabooLib.getAwakenedClasses().get("taboolib.platform.BungeeExecutor");
                o.getClass().getDeclaredMethod("start").invoke(o);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        // 再次判断插件是否关闭
        // 因为插件可能在 onEnable() 下关闭
        if (!TabooLib.isStopped()) {
            // 创建调度器，执行 onActive() 方法
            BungeeCord.getInstance().getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    // 生命周期任务
                    TabooLib.lifeCycle(LifeCycle.ACTIVE);
                    // 调用 Plugin 实现的 onActive() 方法
                    if (pluginInstance != null) {
                        pluginInstance.onActive();
                    }
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onDisable() {
        // 在插件未关闭的前提下，执行 onDisable() 方法
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onDisable();
        }
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.DISABLE);
    }

    @NotNull
    @Override
    public File getFile() {
        return super.getFile();
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return pluginInstance;
    }

    @NotNull
    public static BungeePlugin getInstance() {
        return instance;
    }
}
