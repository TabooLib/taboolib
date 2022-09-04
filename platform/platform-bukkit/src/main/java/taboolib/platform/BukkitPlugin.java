package taboolib.platform;

import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;
import org.tabooproject.reflex.Reflex;

import java.io.File;
import java.util.Set;

/**
 * TabooLib
 * taboolib.platform.BukkitPlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"Convert2Lambda", "ConstantConditions"})
@PlatformSide(Platform.BUKKIT)
public class BukkitPlugin extends JavaPlugin {

    @Nullable
    private static Plugin pluginInstance;
    private static BukkitPlugin instance;

    static {
        TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.BUKKIT);
        if (TabooLibCommon.isKotlinEnvironment()) {
            pluginInstance = Project1Kt.findImplementation(Plugin.class);
        }
    }

    public BukkitPlugin() {
        instance = this;
        injectAccess();
        TabooLibCommon.lifeCycle(LifeCycle.INIT);
    }

    @Override
    public void onLoad() {
        TabooLibCommon.lifeCycle(LifeCycle.LOAD);
        if (pluginInstance == null) {
            pluginInstance = Project1Kt.findImplementation(Plugin.class);
        }
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onLoad();
        }
    }

    @Override
    public void onEnable() {
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
        if (!TabooLibCommon.isStopped()) {
            Bukkit.getScheduler().runTask(this, new Runnable() {
                @Override
                public void run() {
                    TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
                    if (pluginInstance != null) {
                        pluginInstance.onActive();
                    }
                }
            });
        }
    }

    @Override
    public void onDisable() {
        TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onDisable();
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
        try {
            if (pluginInstance instanceof BukkitWorldGenerator) {
                return ((BukkitWorldGenerator) pluginInstance).getDefaultWorldGenerator(worldName, id);
            }
        } catch (NoClassDefFoundError ignored) {
        }
        return null;
    }

    @NotNull
    @Override
    public File getFile() {
        return super.getFile();
    }

    @NotNull
    public static BukkitPlugin getInstance() {
        return instance;
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return pluginInstance;
    }

    /**
     * 移除 Spigot 的访问警告：
     * Loaded class {0} from {1} which is not a depend, softdepend or loadbefore of this plugin
     */
    static void injectAccess() {
        try {
            PluginDescriptionFile description = Reflex.Companion.getProperty(BukkitPlugin.class.getClassLoader(), "description", false, true, false);
            Set<String> accessSelf = Reflex.Companion.getProperty(BukkitPlugin.class.getClassLoader(), "seenIllegalAccess", false, true, false);
            for (org.bukkit.plugin.Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.getClass().getName().endsWith("platform.BukkitPlugin")) {
                    Set<String> accessOther = Reflex.Companion.getProperty(plugin.getClass().getClassLoader(), "seenIllegalAccess", false, true, false);
                    accessOther.add(description.getName());
                    accessSelf.add(plugin.getName());
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
