package taboolib.platform;

import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.PluginContainer;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLib;
import taboolib.common.io.ClassInstanceKt;
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
@org.spongepowered.api.plugin.Plugin(
        id = "@plugin_id@",
        name = "@plugin_name@",
        version = "@plugin_version@"
)
@PlatformSide(Platform.SPONGE_API_7)
public class Sponge7Plugin {

    @Nullable
    private static Plugin pluginInstance;
    private static Sponge7Plugin instance;

    @Inject
    private PluginContainer pluginContainer;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path pluginConfigDir;

    static {
        TabooLib.lifeCycle(LifeCycle.CONST, Platform.SPONGE_API_7);
        if (TabooLib.isKotlinEnvironment()) {
            pluginInstance = ClassInstanceKt.findInstanceFromPlatform(Plugin.class);
        }
    }

    public Sponge7Plugin() {
        instance = this;
    }

    @Listener
    public void e(GameConstructionEvent e) {
        TabooLib.lifeCycle(LifeCycle.INIT);
    }

    @Listener
    public void e(GamePreInitializationEvent e) {
        TabooLib.lifeCycle(LifeCycle.LOAD);
        if (pluginInstance == null) {
            pluginInstance = ClassInstanceKt.findInstanceFromPlatform(Plugin.class);
        }
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onLoad();
        }
    }

    @Listener
    public void e(GameInitializationEvent e) {
        TabooLib.lifeCycle(LifeCycle.ENABLE);
        if (!TabooLib.isStopped()) {
            if (pluginInstance != null) {
                pluginInstance.onEnable();
            }
            try {
                ExecutorKt.startNow();
            } catch (NoClassDefFoundError ignored) {
            }
        }
    }

    @Listener
    public void e(GameStartedServerEvent e) {
        TabooLib.lifeCycle(LifeCycle.ACTIVE);
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onActive();
        }
    }

    @Listener
    public void e(GameStoppedServerEvent e) {
        TabooLib.lifeCycle(LifeCycle.DISABLE);
        if (pluginInstance != null && !TabooLib.isStopped()) {
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
