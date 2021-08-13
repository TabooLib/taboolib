package taboolib.platform;

import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.PluginContainer;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.Project1Kt;
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
@org.spongepowered.api.plugin.Plugin(
        id = "@plugin_id@",
        name = "@plugin_name@",
        version = "@plugin_version@"
)
@PlatformSide(Platform.SPONGE_API_7)
public class Sponge7Plugin {

    @Nullable
    private static final Plugin pluginInstance;
    private static Sponge7Plugin instance;

    @Inject
    private PluginContainer pluginContainer;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path pluginConfigDir;

    static {
        TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.SPONGE_API_7);
        pluginInstance = Project1Kt.findImplementation(Plugin.class);
    }

    public Sponge7Plugin() {
        instance = this;
        TabooLibCommon.lifeCycle(LifeCycle.INIT);
    }

    @Listener
    private void e(GamePreInitializationEvent e) {
        TabooLibCommon.lifeCycle(LifeCycle.LOAD);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onLoad();
        }
    }

    @Listener
    private void e(GameInitializationEvent e) {
        TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onEnable();
        }
        FunctionKt.startExecutor();
    }

    @Listener
    private void e(GameStartedServerEvent e) {
        TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onActive();
        }
    }

    @Listener
    private void e(GameStoppedServerEvent e) {
        TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onDisable();
        }
    }

    @NotNull
    public static Sponge7Plugin getInstance() {
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
        return pluginConfigDir.resolve(pluginContainer.getId()).toFile();
    }
}
