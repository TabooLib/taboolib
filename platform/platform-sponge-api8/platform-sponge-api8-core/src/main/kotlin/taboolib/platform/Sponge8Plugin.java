package taboolib.platform;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLib;
import taboolib.common.boot.Environments;
import taboolib.common.io.ClassInstanceKt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;

import java.io.File;
import java.nio.file.Path;

/**
 * TabooLib
 * taboolib.platform.SpongePlugin
 *
 * @author sky
 * @since 2021/6/26 8:39 下午
 */
@org.spongepowered.plugin.builtin.jvm.Plugin("@plugin_id@")
@PlatformSide(Platform.SPONGE_API_8)
public class Sponge8Plugin {

    @Nullable
    private static Plugin instanceDelegate;
    private static Sponge8Plugin instance;

    private final PluginContainer pluginContainer;
    private final Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path pluginConfigDir;

    static {
        TabooLib.booster().proceed(LifeCycle.CONST, Platform.SPONGE_API_8);
        setupPluginInstance();
    }

    @Inject
    public Sponge8Plugin(final PluginContainer pluginContainer, final Logger logger) {
        this.pluginContainer = pluginContainer;
        this.logger = logger;
        instance = this;
    }

    // 2021/7/7 可能存在争议，不确定其他插件是否会触发该事件
    // It should not trigger by other plugins, as I asked in the discord channel
    @Listener
    public void e(final ConstructPluginEvent e) {
        TabooLib.booster().proceed(LifeCycle.INIT);
        TabooLib.booster().proceed(LifeCycle.LOAD);
        setupPluginInstance();
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onLoad();
        }
    }

    @Listener
    public void e(final StartingEngineEvent<Server> e) {
        TabooLib.booster().proceed(LifeCycle.ENABLE);
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onEnable();
        }
    }

    @Listener
    public void e(final StartedEngineEvent<Server> e) {
        TabooLib.booster().proceed(LifeCycle.ACTIVE);
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onActive();
        }
    }

    @Listener
    public void e(final StoppingEngineEvent<Server> e) {
        TabooLib.booster().proceed(LifeCycle.DISABLE);
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onDisable();
        }
    }

    @NotNull
    public static Sponge8Plugin getInstance() {
        return instance;
    }

    @Nullable
    public static Plugin getInstanceDelegate() {
        return instanceDelegate;
    }

    @NotNull
    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    @NotNull
    public File getPluginConfigDir() {
        return pluginConfigDir.resolve(pluginContainer.metadata().id()).toFile();
    }

    @NotNull
    public Logger getLogger() {
        return logger;
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
