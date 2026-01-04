package taboolib.platform;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.lang.reflect.InvocationTargetException;
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

    public static Method GET_ENTITY_SCHEDULER;

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
            // 获取 EntityScheduler
            Method getEntitySchedulerMethod = Entity.class.getDeclaredMethod("getEntityScheduler");
            getEntitySchedulerMethod.setAccessible(true);
            GET_ENTITY_SCHEDULER = getEntitySchedulerMethod;
        } catch (Throwable ignored) {
        }
    }

    public static EntityScheduler getEntityScheduler(final Entity entity) throws InvocationTargetException, IllegalAccessException {
        return (EntityScheduler) GET_ENTITY_SCHEDULER.invoke(entity);
    }
}
