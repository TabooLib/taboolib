package taboolib.platform;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.plugin.PluginContainer;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.IOKt;
import taboolib.common.platform.FunctionKt;
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
@org.spongepowered.plugin.jvm.Plugin("@plugin_id@")
@PlatformSide(Platform.SPONGE_API_8)
public class SpongePlugin {

    @Nullable
    private static final Plugin pluginInstance;
    private static SpongePlugin instance;

    private final PluginContainer pluginContainer;
    private final Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path pluginConfigDir;

    static {
        TabooLibCommon.lifeCycle(LifeCycle.CONST);
        pluginInstance = IOKt.findInstance(Plugin.class);
    }

    @Inject
    public SpongePlugin(final PluginContainer pluginContainer, final Logger logger) {
        instance = this;
        this.pluginContainer = pluginContainer;
        this.logger = logger;
        TabooLibCommon.lifeCycle(LifeCycle.INIT);

    }

    // TODO: 2021/7/7 可能存在争议，不确定其他插件是否会触发该事件
    // It should not trigger by other plugins, as I asked in the discord channel
    @Listener
    private void e(final ConstructPluginEvent e) {
        TabooLibCommon.lifeCycle(LifeCycle.LOAD);
        if (pluginInstance != null) {
            pluginInstance.onLoad();

        }
    }

    @Listener
    private void e(final StartingEngineEvent<Server> e) {
        TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
        if (pluginInstance != null) {
            pluginInstance.onEnable();
        }
        FunctionKt.startExecutor();
    }

    @Listener
    private void e(final StartedEngineEvent<Server> e) {
        TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
        if (pluginInstance != null) {
            pluginInstance.onActive();
        }
    }

    @Listener
    private void e(final StoppingEngineEvent<Server> e) {
        TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
        if (pluginInstance != null) {
            pluginInstance.onDisable();
        }
    }

    @NotNull
    public static SpongePlugin getInstance() {
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
        return pluginConfigDir.toFile();
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }
}
