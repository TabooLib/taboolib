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
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.CommonKt;

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
    private static final Plugin pluginInstance;
    private static VelocityPlugin instance;

    static {
        TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.VELOCITY);
        pluginInstance = Project1Kt.findImplementation(Plugin.class);
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
        TabooLibCommon.lifeCycle(LifeCycle.INIT);
    }

    @Subscribe
    public void e(ProxyInitializeEvent e) {
        TabooLibCommon.lifeCycle(LifeCycle.LOAD);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onLoad();
        }
        TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onEnable();
            server.getScheduler().buildTask(this, new Runnable() {
                @Override
                public void run() {
                    TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
                    pluginInstance.onActive();
                }
            }).schedule();
        }
        CommonKt.startExecutor();
    }

    @Subscribe
    public void e(ProxyShutdownEvent e) {
        TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
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
