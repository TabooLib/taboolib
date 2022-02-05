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
import taboolib.common.boot.Environments;
import taboolib.common.io.ClassInstanceKt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;

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
    private static Plugin instanceDelegate;
    private static VelocityPlugin instance;

    static {
        TabooLib.booster().proceed(LifeCycle.CONST, Platform.VELOCITY);
        setupPluginInstance();
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
        TabooLib.booster().proceed(LifeCycle.INIT);
    }

    @Subscribe
    public void e(ProxyInitializeEvent e) {
        if (isRunning()) {
            TabooLib.booster().proceed(LifeCycle.LOAD);
            if (instanceDelegate == null) {
                instanceDelegate = ClassInstanceKt.findInstanceFromPlatform(Plugin.class);
            }
            if (instanceDelegate != null) {
                instanceDelegate.onLoad();
            }
        }
        if (isRunning()) {
            TabooLib.booster().proceed(LifeCycle.ENABLE);
            if (instanceDelegate != null) {
                instanceDelegate.onEnable();
            }
        }
        if (isRunning()) {
            server.getScheduler().buildTask(this, new Runnable() {
                @Override
                public void run() {
                    TabooLib.booster().proceed(LifeCycle.ACTIVE);
                    if (instanceDelegate != null) {
                        instanceDelegate.onActive();
                    }
                }
            }).schedule();
        }
    }

    @Subscribe
    public void e(ProxyShutdownEvent e) {
        TabooLib.booster().proceed(LifeCycle.DISABLE);
        if (instanceDelegate != null && isRunning()) {
            instanceDelegate.onDisable();
        }
    }

    @NotNull
    public static VelocityPlugin getInstance() {
        return instance;
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return instanceDelegate;
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

    static boolean isRunning() {
        return !TabooLib.monitor().isShutdown();
    }

    static void setupPluginInstance() {
        if (Environments.isKotlin() && instanceDelegate == null) {
            instanceDelegate = ClassInstanceKt.findInstanceFromPlatform(Plugin.class);
        }
    }
}
