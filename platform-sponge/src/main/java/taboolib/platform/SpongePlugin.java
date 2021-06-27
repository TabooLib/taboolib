package taboolib.platform;

import com.google.inject.Inject;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.PluginContainer;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.IOKt;
import taboolib.common.platform.FunctionKt;
import taboolib.common.platform.Plugin;

import java.io.File;

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
public class SpongePlugin {

    @Nullable
    private static final Plugin pluginInstance;
    private static SpongePlugin instance;

    @Inject
    private PluginContainer pluginContainer;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File pluginConfigDir;

    static {
        TabooLibCommon.init();
        pluginInstance = IOKt.findInstance(Plugin.class);
    }

    public SpongePlugin() {
        instance = this;
    }

    @Listener
    private void e(GameConstructionEvent e) {
        if (pluginInstance != null) {
            pluginInstance.onLoad();
        }
    }

    @Listener
    private void e(GameInitializationEvent e) {
        if (pluginInstance != null) {
            pluginInstance.onEnable();
        }
        FunctionKt.startExecutor();
    }

    @Listener
    private void e(GameStartedServerEvent e) {
        if (pluginInstance != null) {
            pluginInstance.onActive();
        }
    }

    @Listener
    private void e(GameStoppedServerEvent e) {
        if (pluginInstance != null) {
            pluginInstance.onDisable();
        }
        TabooLibCommon.cancel();
    }

    public static SpongePlugin getInstance() {
        return instance;
    }

    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    public File getPluginConfigDir() {
        return pluginConfigDir;
    }
}
