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
import taboolib.common.TabooLibCommon;
import taboolib.common.classloader.IsolatedClassLoader;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.net.URL;
import java.nio.file.Path;

/**
 * TabooLib
 * taboolib.platform.VelocityPlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"Convert2Lambda", "DuplicatedCode", "CallToPrintStackTrace"})
@com.velocitypowered.api.plugin.Plugin(
        id = "@plugin_id@",
        name = "@plugin_name@",
        version = "@plugin_version@"
)
@PlatformSide(Platform.VELOCITY)
public class VelocityPlugin {

    @Nullable
    private static Plugin pluginInstance;
    private static VelocityPlugin instance;
    private static Class<?> delegateClass;
    private static Object delegateObject;
    @Nullable
    private static IsolatedClassLoader isolatedClassLoader;

    static {
        if (IsolatedClassLoader.isEnabled()) {
            try {
                IsolatedClassLoader loader = new IsolatedClassLoader(
                        new URL[]{VelocityPlugin.class.getProtectionDomain().getCodeSource().getLocation()},
                        VelocityPlugin.class.getClassLoader()
                );
                loader.addExcludedClass("taboolib.platform.VelocityPlugin");
                isolatedClassLoader = loader;
                delegateClass = Class.forName("taboolib.platform.VelocityPluginDelegate", true, loader);
                delegateObject = delegateClass.getConstructor().newInstance();
                delegateClass.getMethod("onConst").invoke(delegateObject);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.VELOCITY);
            if (TabooLibCommon.isKotlinEnvironment()) {
                pluginInstance = Project1Kt.findImplementation(Plugin.class);
            }
        }
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

        if (IsolatedClassLoader.isEnabled()) {
            try {
                delegateClass.getMethod("onInit").invoke(delegateObject);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            TabooLibCommon.lifeCycle(LifeCycle.INIT);
        }
    }

    @Subscribe
    public void e(ProxyInitializeEvent e) {
        if (IsolatedClassLoader.isEnabled()) {
            try {
                delegateClass.getMethod("onLoad").invoke(delegateObject);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            if (!TabooLibCommon.isStopped()) {
                TabooLibCommon.lifeCycle(LifeCycle.LOAD);
                if (pluginInstance == null) {
                    pluginInstance = Project1Kt.findImplementation(Plugin.class);
                }
                if (pluginInstance != null) {
                    pluginInstance.onLoad();
                }
            }
            if (!TabooLibCommon.isStopped()) {
                TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
                if (pluginInstance != null) {
                    pluginInstance.onEnable();
                }
                try {
                    ExecutorKt.startExecutor();
                } catch (NoClassDefFoundError ignored) {
                }
            }
            if (!TabooLibCommon.isStopped()) {
                server.getScheduler().buildTask(this, new Runnable() {
                    @Override
                    public void run() {
                        TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
                        if (pluginInstance != null) {
                            pluginInstance.onActive();
                        }
                    }
                }).schedule();
            }
        }
    }

    @Subscribe
    public void e(ProxyShutdownEvent e) {
        if (IsolatedClassLoader.isEnabled()) {
            try {
                delegateClass.getMethod("onDisable").invoke(delegateObject);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            if (pluginInstance != null && !TabooLibCommon.isStopped()) {
                pluginInstance.onDisable();
            }
            TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
        }
    }

    @NotNull
    public static VelocityPlugin getInstance() {
        return instance;
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return pluginInstance;
    }

    @NotNull
    public ProxyServer getServer() {
        return server;
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }

    @NotNull
    public Path getConfigDirectory() {
        return configDirectory;
    }

    @Nullable
    public static IsolatedClassLoader getIsolatedClassLoader() {
        return isolatedClassLoader;
    }
}
