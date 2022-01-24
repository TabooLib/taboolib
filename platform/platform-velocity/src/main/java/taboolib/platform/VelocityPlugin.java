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
import taboolib.common.TabooLib;
import taboolib.common.io.ClassInstanceKt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.nio.file.Path;

/**
 * TabooLib
 * taboolib.platform.VelocityPlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings("Convert2Lambda")
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

    static {
        TabooLib.lifeCycle(LifeCycle.CONST, Platform.VELOCITY);
        if (TabooLib.isKotlinEnvironment()) {
            pluginInstance = ClassInstanceKt.findImplementation(Plugin.class);
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
        TabooLib.lifeCycle(LifeCycle.INIT);
    }

    @Subscribe
    public void e(ProxyInitializeEvent e) {
        if (!TabooLib.isStopped()) {
            TabooLib.lifeCycle(LifeCycle.LOAD);
            if (pluginInstance == null) {
                pluginInstance = ClassInstanceKt.findImplementation(Plugin.class);
            }
            if (pluginInstance != null) {
                pluginInstance.onLoad();
            }
        }
        if (!TabooLib.isStopped()) {
            TabooLib.lifeCycle(LifeCycle.ENABLE);
            if (pluginInstance != null) {
                pluginInstance.onEnable();
            }
            try {
                ExecutorKt.startExecutor();
            } catch (NoClassDefFoundError ignored) {
            }
        }
        if (!TabooLib.isStopped()) {
            server.getScheduler().buildTask(this, new Runnable() {
                @Override
                public void run() {
                    TabooLib.lifeCycle(LifeCycle.ACTIVE);
                    if (pluginInstance != null) {
                        pluginInstance.onActive();
                    }
                }
            }).schedule();
        }
    }

    @Subscribe
    public void e(ProxyShutdownEvent e) {
        TabooLib.lifeCycle(LifeCycle.DISABLE);
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onDisable();
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
}
