package taboolib.platform;

import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.PluginContainer;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.env.IsolatedClassLoader;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.io.File;
import java.net.URL;
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
    private static Class<?> delegateClass;
    private static Object delegateObject;
    @Nullable
    private static IsolatedClassLoader isolatedClassLoader;

    @Inject
    private PluginContainer pluginContainer;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path pluginConfigDir;

    static {
	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            IsolatedClassLoader loader = new IsolatedClassLoader(
	                    new URL[]{Sponge7Plugin.class.getProtectionDomain().getCodeSource().getLocation()},
	                    Sponge7Plugin.class.getClassLoader()
	            );
	            loader.addExcludedClass("taboolib.platform.Sponge7Plugin");
	            isolatedClassLoader = loader;
	            delegateClass = Class.forName("taboolib.platform.Sponge7PluginDelegate", true, loader);
	            delegateObject = delegateClass.getConstructor().newInstance();
	            delegateClass.getMethod("onConst").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
	        TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.SPONGE_API_7);
	        if (TabooLibCommon.isKotlinEnvironment()) {
	            pluginInstance = Project1Kt.findImplementation(Plugin.class);
	        }
	    }
    }

    public Sponge7Plugin() {
        instance = this;
    }

    @Listener
    public void e(GameConstructionEvent e) {
	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            delegateClass.getMethod("onInit").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
	        TabooLibCommon.lifeCycle(LifeCycle.INIT);
	    }
    }

    @Listener
    public void e(GamePreInitializationEvent e) {
	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            delegateClass.getMethod("onLoad").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
	        TabooLibCommon.lifeCycle(LifeCycle.LOAD);
	        if (pluginInstance == null) {
	            pluginInstance = Project1Kt.findImplementation(Plugin.class);
	        }
	        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
	            pluginInstance.onLoad();
	        }
	    }
    }

    @Listener
    public void e(GameInitializationEvent e) {
	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            delegateClass.getMethod("onEnable").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
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
    }

    @Listener
    public void e(GameStartedServerEvent e) {
	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            delegateClass.getMethod("onActive").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
	        TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
	        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
	            pluginInstance.onActive();
	        }
	    }
    }

    @Listener
    public void e(GameStoppedServerEvent e) {
	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            delegateClass.getMethod("onDisable").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
	        TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
	        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
	            pluginInstance.onDisable();
	        }
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

	@Nullable
	public static IsolatedClassLoader getIsolatedClassLoader() {
		return isolatedClassLoader;
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
