package taboolib.expansion.folialib;

import taboolib.expansion.folia.Folia;
import taboolib.expansion.folialib.Enum.ServerType;
import taboolib.expansion.folialib.Wrapper.Scheduler;
import taboolib.expansion.folialib.Wrapper.SchedulerWrapper.BukkitScheduler;
import taboolib.expansion.folialib.Wrapper.SchedulerWrapper.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**
 * FoliaLib
 * <a href="https://github.com/xgpjun/FoliaLib">FoliaLib</a>
 * 本部分代码来自于此开源项目遵守MIT协议
 */
public class FoliaLibAPI {
    private final Plugin plugin;
    private final ServerType serverType;

    /**
     * 获得API实例
     */
    public FoliaLibAPI() {
        serverType = ServerType.getServerType();
        this.plugin = Folia.INSTANCE.getPlugin();
    }

    /**
     * 传送实体
     *
     * @param entity 需要传送的实体
     * @param target 目的地
     * @return 传送结果
     */
    public boolean teleport(Entity entity, Location target) {
        return teleport(entity, target, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    /**
     * 传送实体
     *
     * @param entity 需要传送的实体
     * @param target 传送目的地
     * @param cause  传送原因
     * @return 传送结果
     */
    public boolean teleport(Entity entity, Location target, PlayerTeleportEvent.TeleportCause cause) {
        if (Objects.requireNonNull(serverType) == ServerType.FOLIA) {
            return entity.teleportAsync(target, cause).isDone();
        }
        return entity.teleport(target, cause);
    }

    /**
     * 获得调度程序管理器, 参数仅影在Folia中的实现, 无参代表getAsyncScheduler()。
     *
     * @return 调度管理器
     */
    public Scheduler getScheduler() {
        if (Objects.requireNonNull(serverType) == ServerType.FOLIA) {
            return new FoliaScheduler(plugin, false);
        }
        return new BukkitScheduler(plugin);
    }

    /**
     * 获得调度程序管理器, 参数仅影在Folia中的实现, 此处获得实体调度器.
     *
     * @param entity  操作的实体
     * @param retired 回调函数. 当执行时实体变为null时执行的方法。
     * @return 调度管理器
     */
    public Scheduler getScheduler(Entity entity, Runnable retired) {
        if (Objects.requireNonNull(serverType) == ServerType.FOLIA) {
            return new FoliaScheduler(plugin, entity, retired);
        }
        return new BukkitScheduler(plugin);
    }

    /**
     * 获得调度程序管理器, 参数仅影在Folia中的实现, 此处获得区块调度器.
     *
     * @param location 区域的位置
     * @return 调度管理器
     */
    public Scheduler getScheduler(Location location) {
        if (Objects.requireNonNull(serverType) == ServerType.FOLIA) {
            return new FoliaScheduler(plugin, location);
        }
        return new BukkitScheduler(plugin);
    }

    /**
     * 获得调度程序管理器, 参数仅影在Folia中的实现, 此处获得全局调度器.
     *
     * @param isGlobal 是否为全局， 如为false则等同无参。
     * @return 调度管理器
     */
    public Scheduler getScheduler(boolean isGlobal) {
        if (Objects.requireNonNull(serverType) == ServerType.FOLIA) {
            return new FoliaScheduler(plugin, isGlobal);
        }
        return new BukkitScheduler(plugin);
    }

    /**
     * 取消所有调度任务
     *
     * @param plugin 你的插件实例
     */
    public void cancelTask(Plugin plugin) {
        if (Objects.requireNonNull(serverType) == ServerType.FOLIA) {
            Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
            Bukkit.getAsyncScheduler().cancelTasks(plugin);
        } else {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }
}
