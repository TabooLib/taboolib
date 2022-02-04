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
import taboolib.common.TabooLibCommon;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

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
@PlatformSide(Platform.SPONGE_API_9)
public class Sponge9Plugin {

    @Nullable
    private static Plugin pluginInstance;
    private static Sponge9Plugin instance;

    private final PluginContainer pluginContainer;
    private final Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path pluginConfigDir;

    static {
        TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.SPONGE_API_9);
        if (TabooLibCommon.isKotlinEnvironment()) {
            pluginInstance = Project1Kt.findImplementation(Plugin.class);
        }
    }

    @Inject
    public Sponge9Plugin(final PluginContainer pluginContainer, final Logger logger) {
        this.pluginContainer = pluginContainer;
        this.logger = logger;
        instance = this;
    }

    // 2021/7/7 可能存在争议，不确定其他插件是否会触发该事件
    // It should not trigger by other plugins, as I asked in the discord channel
    @Listener
    public void e(final ConstructPluginEvent e) {
        TabooLibCommon.lifeCycle(LifeCycle.INIT);
        TabooLibCommon.lifeCycle(LifeCycle.LOAD);
        if (pluginInstance == null) {
            pluginInstance = Project1Kt.findImplementation(Plugin.class);
        }
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onLoad();
        }
    }

    @Listener
    public void e(final StartingEngineEvent<Server> e) {
        TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
        if (!TabooLibCommon.isStopped()) {
            if (pluginInstance != null) {
                pluginInstance.onEnable();
            }
            try {
                ExecutorKt.startExecutor();
            } catch (NoClassDefFoundError ignored) {
            }
        }
    }

    @Listener
    public void e(final StartedEngineEvent<Server> e) {
        TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onActive();
        }
    }

    @Listener
    public void e(final StoppingEngineEvent<Server> e) {
        TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onDisable();
        }
    }

    @NotNull
    public static Sponge9Plugin getInstance() {
        return instance;
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return pluginInstance;
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
}
