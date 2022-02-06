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
@org.spongepowered.api.plugin.Plugin(
        id = "@plugin_id@",
        name = "@plugin_name@",
        version = "@plugin_version@"
)
@PlatformSide(Platform.SPONGE_API_7)
public class Sponge7Plugin {

    @Nullable
    private static Plugin instanceDelegate;
    private static Sponge7Plugin instance;

    @Inject
    private PluginContainer pluginContainer;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path pluginConfigDir;

    static {
        TabooLib.booster().proceed(LifeCycle.CONST, Platform.SPONGE_API_7);
        setupPluginInstance();
    }

    public Sponge7Plugin() {
        instance = this;
    }

    @Listener
    public void e(GameConstructionEvent e) {
        TabooLib.booster().proceed(LifeCycle.INIT);
    }

    @Listener
    public void e(GamePreInitializationEvent e) {
        TabooLib.booster().proceed(LifeCycle.LOAD);
        setupPluginInstance();
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onLoad();
        }
    }

    @Listener
    public void e(GameInitializationEvent e) {
        TabooLib.booster().proceed(LifeCycle.ENABLE);
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onEnable();
        }
    }

    @Listener
    public void e(GameStartedServerEvent e) {
        TabooLib.booster().proceed(LifeCycle.ACTIVE);
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onActive();
        }
    }

    @Listener
    public void e(GameStoppedServerEvent e) {
        TabooLib.booster().proceed(LifeCycle.DISABLE);
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onDisable();
        }
    }

    @NotNull
    public static Sponge7Plugin getInstance() {
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
        return pluginConfigDir.resolve(pluginContainer.getId()).toFile();
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
