package taboolib.platform;

import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.IOKt;
import taboolib.common.platform.FunctionKt;
import taboolib.plugin.Plugin;

import java.io.File;

/**
 * TabooLib
 * taboolib.platform.BukkitPlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"Anonymous2MethodRef", "Convert2Lambda"})
public class BukkitPlugin extends JavaPlugin {

    @Nullable
    private static final Plugin pluginInstance;

    static {
        TabooLibCommon.init();
        pluginInstance = IOKt.findInstance(Plugin.class);
    }

    @Override
    public void onLoad() {
        if (pluginInstance != null) {
            pluginInstance.onLoad();
        }
    }

    @Override
    public void onEnable() {
        if (pluginInstance != null) {
            pluginInstance.onEnable();
            Bukkit.getScheduler().runTask(this, new Runnable() {
                @Override
                public void run() {
                    pluginInstance.onActive();
                }
            });
        }
        FunctionKt.startExecutor();
    }

    @Override
    public void onDisable() {
        if (pluginInstance != null) {
            pluginInstance.onDisable();
        }
        TabooLibCommon.cancel();
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (pluginInstance instanceof BukkitWorldGenerator) {
            return ((BukkitWorldGenerator) pluginInstance).getDefaultWorldGenerator(worldName, id);
        }
        return null;
    }

    @Override
    public File getFile() {
        return super.getFile();
    }
}
