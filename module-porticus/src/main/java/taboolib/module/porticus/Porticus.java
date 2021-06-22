package taboolib.module.porticus;

import com.google.common.collect.Lists;
import taboolib.common.env.RuntimeDependency;
import taboolib.common.platform.Awake;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;

import java.util.List;

/**
 * Porticus API 通用入口
 *
 * @author 坏黑
 * @since 2020-10-15
 */
@RuntimeDependency(value = "com.github.ben-manes.caffeine:caffeine:3.0.2", test = "com.github.benmanes.caffeine.cache.Cache")
@Awake
@PlatformSide({Platform.BUKKIT, Platform.BUNGEE})
public class Porticus {

    private static final List<PorticusMission> missions = Lists.newCopyOnWriteArrayList();
    private static API api;

    static {
        try {
            Class.forName("org.bukkit.Bukkit");
            api = new taboolib.module.porticus.bukkitside.PorticusAPI();
        } catch (Throwable ignored) {
        }
        try {
            Class.forName("net.md_5.bungee.BungeeCord");
            api = new taboolib.module.porticus.bungeeside.PorticusAPI();
        } catch (Throwable ignored) {
        }
    }

    Porticus() {
    }

    /**
     * 获取正在运行的通讯任务
     */
    public static List<PorticusMission> getMissions() {
        return missions;
    }

    /**
     * 获取 Porticus API
     */
    public static API getAPI() {
        return api;
    }
}
