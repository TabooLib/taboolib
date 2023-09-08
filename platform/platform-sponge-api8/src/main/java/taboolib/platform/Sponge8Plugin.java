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
@org.spongepowered.plugin.builtin.jvm.Plugin("@plugin_id@")
@PlatformSide(Platform.SPONGE_API_8)
public class Sponge8Plugin {

    @Nullable
    private static Plugin pluginInstance;
    private static Sponge8Plugin instance;
    private static Class<?> delegateClass;
    private static Object delegateObject;
    @Nullable
    private static IsolatedClassLoader isolatedClassLoader;
    
    private final PluginContainer pluginContainer;
    private final Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path pluginConfigDir;

    static {
	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            IsolatedClassLoader loader = new IsolatedClassLoader(
	                    new URL[]{Sponge8Plugin.class.getProtectionDomain().getCodeSource().getLocation()},
	                    Sponge8Plugin.class.getClassLoader()
	            );
                loader.addExcludedClass("taboolib.platform.Sponge8Plugin");
                isolatedClassLoader = loader;
	            delegateClass = Class.forName("taboolib.platform.Sponge8PluginDelegate", true, loader);
	            delegateObject = delegateClass.getConstructor().newInstance();
	            delegateClass.getMethod("onConst").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
	        TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.SPONGE_API_8);
	        if (TabooLibCommon.isKotlinEnvironment()) {
	            pluginInstance = Project1Kt.findImplementation(Plugin.class);
	        }
	    }
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
	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            delegateClass.getMethod("onLoad").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
	        TabooLibCommon.lifeCycle(LifeCycle.INIT);
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
    public void e(final StartingEngineEvent<Server> e) {
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
    public void e(final StartedEngineEvent<Server> e) {
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
    public void e(final StoppingEngineEvent<Server> e) {
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
    public static Sponge8Plugin getInstance() {
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
        return pluginConfigDir.resolve(pluginContainer.metadata().id()).toFile();
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }
}
