package taboolib.platform;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;

/**
 * TabooLib
 * taboolib.platform.FoliaExecutor
 *
 * @author 坏黑
 * @since 2024/3/9 02:24
 */
@SuppressWarnings("JavaReflectionMemberAccess")
public class FoliaExecutor {

    public static AsyncScheduler ASYNC_SCHEDULER;

    public static RegionScheduler REGION_SCHEDULER;

    public static GlobalRegionScheduler GLOBAL_REGION_SCHEDULER;

    static {
        try {
            // 获取 AsyncScheduler
            Method getAsyncSchedulerMethod = Bukkit.class.getDeclaredMethod("getAsyncScheduler");
            getAsyncSchedulerMethod.setAccessible(true);
            ASYNC_SCHEDULER = (AsyncScheduler) getAsyncSchedulerMethod.invoke(Bukkit.getServer());
            // 获取 RegionScheduler
            Method getRegionSchedulerMethod = Bukkit.class.getDeclaredMethod("getRegionScheduler");
            getRegionSchedulerMethod.setAccessible(true);
            REGION_SCHEDULER = (RegionScheduler) getRegionSchedulerMethod.invoke(Bukkit.getServer());
            // 获取 GlobalRegionScheduler
            Method getGlobalRegionSchedulerMethod = Bukkit.class.getDeclaredMethod("getGlobalRegionScheduler");
            getGlobalRegionSchedulerMethod.setAccessible(true);
            GLOBAL_REGION_SCHEDULER = (GlobalRegionScheduler) getGlobalRegionSchedulerMethod.invoke(Bukkit.getServer());
        } catch (Throwable ignored) {
        }
    }
}
