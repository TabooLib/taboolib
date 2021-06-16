package taboolib.module.porticus;

import com.google.common.collect.Lists;
import taboolib.common.platform.PlatformInstance;
import taboolib.module.dependency.RuntimeDependency;
import taboolib.module.dependency.RuntimeName;
import taboolib.module.dependency.RuntimeTest;

import java.util.List;

/**
 * Porticus API 通用入口
 *
 * @author 坏黑
 * @since 2020-10-15
 */
@RuntimeDependency(group = "com.github.ben-manes.caffeine", id = "caffeine", version = "3.0.2", hash = "e26e04138abe2db5ab227bd2b8b3337f83c9cf03")
@RuntimeName(group = "com.github.ben-manes.caffeine", name = "caffeine (3.0.2)")
@RuntimeTest(group = "com.github.ben-manes.caffeine", path = "com.github.benmanes.caffeine.cache.Cache")
@PlatformInstance
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
