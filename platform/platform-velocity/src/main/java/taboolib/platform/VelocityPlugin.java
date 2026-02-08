package taboolib.platform;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import taboolib.common.LifeCycle;
import taboolib.common.PrimitiveIO;
import taboolib.common.TabooLib;
import taboolib.common.classloader.IsolatedClassLoader;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;

import java.nio.file.Path;

import static taboolib.common.PrimitiveIO.t;

/**
 * TabooLib
 * taboolib.platform.BungeePlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"DuplicatedCode", "CallToPrintStackTrace"})
@PlatformSide(Platform.VELOCITY)
@com.velocitypowered.api.plugin.Plugin(
        id = "@plugin_id@",
        name = "@plugin_name@",
        version = "@plugin_version@"
)
public class VelocityPlugin {

    @Nullable
    private static Plugin pluginInstance;
    private static VelocityPlugin instance;

    static {
        PrimitiveIO.debug("Velocity 插件初始化完成，用时 {0} 毫秒。", TabooLib.execution(() -> {
            try {
                // 初始化 IsolatedClassLoader
                IsolatedClassLoader.init(VelocityPlugin.class);
            } catch (Throwable ex) {
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
            // 生命周期任务
            TabooLib.lifeCycle(LifeCycle.CONST);
            // 检索 TabooLib Plugin 实现
            pluginInstance = Plugin.getInstance();
        }));
    }

    private final ProxyServer server;
    private final Logger logger;
    private final Path configDirectory;

    @Inject
    public VelocityPlugin(final ProxyServer server, final Logger logger, @DataDirectory final Path configDirectory) {
        this.logger = logger;
        this.server = server;
        this.configDirectory = configDirectory;
        instance = this;
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.INIT);
    }

    @Subscribe
    public void e(ProxyInitializeEvent e) {
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.LOAD);
        // 调用 Plugin 实现的 onLoad() 方法
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onLoad();
        }
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
            server.getScheduler().buildTask(this, () -> {
                // 生命周期任务
                TabooLib.lifeCycle(LifeCycle.ACTIVE);
                // 调用 Plugin 实现的 onActive() 方法
                if (pluginInstance != null) {
                    pluginInstance.onActive();
                }
            }).schedule();
        }
    }

    @Subscribe
    public void e(ProxyShutdownEvent e) {
        // 在插件未关闭的前提下，执行 onDisable() 方法
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onDisable();
        }
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.DISABLE);
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return pluginInstance;
    }

    @NotNull
    public static VelocityPlugin getInstance() {
        return instance;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getConfigDirectory() {
        return configDirectory;
    }
}
