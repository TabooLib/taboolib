package taboolib.platform;

import net.md_5.bungee.BungeeCord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.util.concurrent.TimeUnit;

/**
 * TabooLib
 * taboolib.platform.BungeePlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"Convert2Lambda"})
@PlatformSide(Platform.BUNGEE)
public class BungeePlugin extends net.md_5.bungee.api.plugin.Plugin {

    @Nullable
    private static Plugin pluginInstance;
    private static BungeePlugin instance;

    static {
        TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.BUNGEE);
        // 搜索 Plugin 实现
        if (TabooLibCommon.isKotlinEnvironment()) {
            pluginInstance = Project1Kt.findImplementation(Plugin.class);
        }
    }

    public BungeePlugin() {
        instance = this;
        // 生命周期
        TabooLibCommon.lifeCycle(LifeCycle.INIT);
    }

    @Override
    public void onLoad() {
        TabooLibCommon.lifeCycle(LifeCycle.LOAD);
        // 再次尝试搜索 Plugin 实现
        if (pluginInstance == null) {
            pluginInstance = Project1Kt.findImplementation(Plugin.class);
        }
        // 调用 Plugin 实现的 onLoad() 方法
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onLoad();
        }
    }

    @Override
    public void onEnable() {
        TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
        // 判断插件是否关闭
        if (!TabooLibCommon.isStopped()) {
            // 调用 onEnable() 方法
            if (pluginInstance != null) {
                pluginInstance.onEnable();
            }
            // 启动调度器
            try {
                ExecutorKt.startExecutor();
            } catch (NoClassDefFoundError ignored) {
            }
        }
        // 再次判断插件是否关闭
        if (!TabooLibCommon.isStopped()) {
            // 创建调度器，执行 onActive() 方法
            BungeeCord.getInstance().getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
                    if (pluginInstance != null) {
                        pluginInstance.onActive();
                    }
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onDisable() {
        TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
        // 在插件未关闭的前提下，执行 onDisable() 方法
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onDisable();
        }
    }

    @NotNull
    public static BungeePlugin getInstance() {
        return instance;
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return pluginInstance;
    }
}
