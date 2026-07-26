package taboolib.platform;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventTask;
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
import taboolib.common.PrimitiveSettings;
import taboolib.common.TabooLib;
import taboolib.common.classloader.IsolatedClassLoader;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

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

    private final ProxyServer server;
    private final Logger logger;
    private final Path configDirectory;
    private final VelocityActivationGate activationGate = new VelocityActivationGate();
    private final AtomicReference<CompletableFuture<Void>> disableFuture = new AtomicReference<>();

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
            server.getScheduler().buildTask(this, () -> activationGate.activate(() -> {
                if (TabooLib.isStopped()) {
                    return;
                }
                // 生命周期任务
                TabooLib.lifeCycle(LifeCycle.ACTIVE);
                // 调用 Plugin 实现的 onActive() 方法
                if (pluginInstance != null) {
                    pluginInstance.onActive();
                }
            })).schedule();
        }
    }

    /**
     * 保留旧的公开方法签名，避免破坏可能存在的反射调用。
     * <p>
     * 注意：该方法已不再带有 {@code @Subscribe}，Velocity 不会再触发它，
     * 关服流程实际由 {@link #eAsync(ProxyShutdownEvent)} 处理——后者能通过
     * {@link EventTask} 向 Velocity 表达异步完成，从而保证 DISABLE 阶段执行完毕后才继续关服。
     */
    public void e(ProxyShutdownEvent e) {
        observeDisable(disableAfterActivation());
    }

    @Subscribe
    public EventTask eAsync(ProxyShutdownEvent e) {
        return EventTask.resumeWhenComplete(disableAfterActivation());
    }

    private CompletableFuture<Void> disableAfterActivation() {
        CompletableFuture<Void> current = disableFuture.get();
        if (current != null) {
            return current;
        }
        CompletableFuture<Void> created = new CompletableFuture<>();
        if (!disableFuture.compareAndSet(null, created)) {
            return disableFuture.get();
        }
        activationGate.close().whenComplete((unused, failure) -> {
            if (failure != null) {
                created.completeExceptionally(failure);
                return;
            }
            try {
                disable();
                created.complete(null);
            } catch (Throwable ex) {
                created.completeExceptionally(ex);
            }
        });
        return created;
    }

    private void observeDisable(CompletableFuture<Void> future) {
        future.whenComplete((unused, failure) -> {
            if (failure != null) {
                try {
                    logger.error("Failed to disable the TabooLib Velocity plugin", failure);
                } catch (Throwable ignored) {
                    try {
                        failure.printStackTrace();
                    } catch (Throwable ignoredAgain) {
                    }
                }
            }
        });
    }

    private void disable() {
        Throwable failure = null;
        // 在插件未关闭的前提下，执行 onDisable() 方法
        if (pluginInstance != null && !TabooLib.isStopped()) {
            try {
                pluginInstance.onDisable();
            } catch (Throwable ex) {
                failure = ex;
            }
        }
        // 生命周期任务必须执行，不能被用户回调异常跳过
        try {
            TabooLib.lifeCycle(LifeCycle.DISABLE);
        } catch (Throwable ex) {
            if (failure == null) {
                failure = ex;
            } else {
                failure.addSuppressed(ex);
            }
        }
        if (failure != null) {
            VelocityPlugin.<RuntimeException>rethrow(failure);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void rethrow(Throwable throwable) throws T {
        throw (T) throwable;
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
