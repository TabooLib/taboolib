package taboolib.platform;

import net.afyer.afybroker.server.Broker;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static taboolib.common.PrimitiveIO.t;

/**
 * TabooLib
 * taboolib.platform.AfyBrokerPlugin
 *
 * @author Ling556
 */
@SuppressWarnings({"Convert2Lambda", "DuplicatedCode", "CallToPrintStackTrace"})
@PlatformSide(Platform.AFYBROKER)
public class AfyBrokerPlugin extends net.afyer.afybroker.server.plugin.Plugin {

    @Nullable
    private static Plugin pluginInstance;
    private static AfyBrokerPlugin instance;
    private final AfyBrokerActiveGate activeGate = new AfyBrokerActiveGate();
    private final AtomicBoolean disabled = new AtomicBoolean();

    static {
        PrimitiveIO.debug("AfyBroker 插件初始化完成，用时 {0} 毫秒。", TabooLib.execution(() -> {
            try {
                // 初始化 IsolatedClassLoader
                IsolatedClassLoader.init(AfyBrokerPlugin.class);
            } catch (Throwable ex) {
                if (PrimitiveSettings.IS_DISABLE_WHEN_PRIMITIVE_LOADER_ERROR) {
                    // 提示信息
                    PrimitiveIO.error(
                            t(
                                    "无法初始化原始加载器，为避免数据丢失，服务器将会被强制关闭！",
                                    "Failed to initialize primitive loader. To avoid data loss, the server will be forced to shut down!"
                            )
                    );
                    // 打印堆栈
                    ex.printStackTrace();
                    try {
                        Thread.sleep(3000);
                    } catch (Throwable ignored) {
                    }
                    // 关闭程序
                    Runtime.getRuntime().halt(-1);
                } else {
                    // 提示信息
                    PrimitiveIO.error(
                            t(
                                    "无法初始化原始加载器，插件 \"{0}\" 将被禁用！",
                                    "Failed to initialize primitive loader, the plugin \"{0}\" will be disabled!"
                            ),
                            PrimitiveIO.getRunningFileName()
                    );
                    // 重抛错误
                    throw ex;
                }
            }
            // 生命周期任务
            TabooLib.lifeCycle(LifeCycle.CONST);
            // 检索 TabooLib Plugin 实现
            pluginInstance = Plugin.getInstance();
        }));
    }

    public AfyBrokerPlugin() {
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
        }
        // 再次判断插件是否关闭
        // 因为插件可能在 onEnable() 下关闭
        if (!TabooLib.isStopped()) {
            // 创建调度器，执行 onActive() 方法
            Broker.getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    activeGate.activate(new Runnable() {
                        @Override
                        public void run() {
                            if (TabooLib.isStopped()) {
                                return;
                            }
                            // 生命周期任务
                            TabooLib.lifeCycle(LifeCycle.ACTIVE);
                            // 调用 Plugin 实现的 onActive() 方法
                            if (pluginInstance != null) {
                                pluginInstance.onActive();
                            }
                        }
                    });
                }
            }, 0, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onDisable() {
        // 第一时间关闭激活入口；若 ACTIVE 正在执行，则在其结束后再进入 DISABLE
        CompletableFuture<Void> activationClosed = activeGate.close();
        if (activationClosed.isDone()) {
            disable();
            return;
        }
        activationClosed.whenComplete((unused, failure) -> {
            if (failure != null) {
                reportDisableFailure(failure);
                return;
            }
            try {
                disable();
            } catch (Throwable ex) {
                reportDisableFailure(ex);
            }
        });
    }

    private void disable() {
        if (!disabled.compareAndSet(false, true)) {
            return;
        }
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
            AfyBrokerPlugin.<RuntimeException>rethrow(failure);
        }
    }

    private void reportDisableFailure(Throwable ex) {
        try {
            PrimitiveIO.error("AfyBroker 平台禁用流程执行异常：{0}", ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
        } catch (Throwable ignored) {
        }
        try {
            ex.printStackTrace();
        } catch (Throwable ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void rethrow(Throwable throwable) throws T {
        throw (T) throwable;
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
    public static AfyBrokerPlugin getInstance() {
        return instance;
    }
}
