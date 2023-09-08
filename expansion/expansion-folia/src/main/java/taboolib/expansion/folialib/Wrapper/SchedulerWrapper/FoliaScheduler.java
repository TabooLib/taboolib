package taboolib.expansion.folialib.Wrapper.SchedulerWrapper;

import taboolib.expansion.folialib.Enum.SchedulerType;
import taboolib.expansion.folialib.Wrapper.Scheduler;
import taboolib.expansion.folialib.Wrapper.Task;
import taboolib.expansion.folialib.Wrapper.TaskWrapper.FoliaTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class FoliaScheduler implements Scheduler {
    private final SchedulerType schedulerType;
    private Location location;
    private Entity entity;
    private Runnable retired;
    private final Plugin plugin;
    public FoliaScheduler(Plugin plugin, boolean isGlobal){
        this.plugin = plugin;
        if(isGlobal)
            schedulerType = SchedulerType.GLOBAL;
        else
            schedulerType = SchedulerType.ASYNC;
    }
    public FoliaScheduler(Plugin plugin,Location location){
        this.plugin = plugin;
        this.location = location;
        schedulerType = SchedulerType.REGION;
    }
    public FoliaScheduler(Plugin plugin, Entity entity,Runnable retired){
        this.plugin = plugin;
        this.entity = entity;
        this.retired =retired;
        schedulerType = SchedulerType.ENTITY;
    }
    @Override
    public Task runTask(@NotNull Runnable runnable) {
        ScheduledTask task1;
        switch (schedulerType){
            case ENTITY: task1 = entity.getScheduler().run(plugin,a-> runnable.run(),retired);break;
            case GLOBAL: task1 = Bukkit.getGlobalRegionScheduler().run(plugin,a-> runnable.run());break;
            case REGION: task1 = Bukkit.getRegionScheduler().run(plugin,location,a-> runnable.run());break;
            case ASYNC:
            default:task1 = Bukkit.getAsyncScheduler().runNow(plugin,a-> runnable.run());break;

        }
        return new FoliaTask(task1);
    }

    @Override
    public Task runTaskLater(@NotNull Runnable runnable, long delay) {
        ScheduledTask task1;
        switch (schedulerType){
            case ENTITY: task1 = entity.getScheduler().runDelayed(plugin,a-> runnable.run(),retired,delay);break;
            case GLOBAL: task1 = Bukkit.getGlobalRegionScheduler().runDelayed(plugin,a-> runnable.run(),delay);break;
            case REGION: task1 = Bukkit.getRegionScheduler().runDelayed(plugin,location,a-> runnable.run(),delay);break;
            case ASYNC:
            default:task1 = Bukkit.getAsyncScheduler().runDelayed(plugin,a-> runnable.run(),delay*50, TimeUnit.MILLISECONDS);break;
        }
        return new FoliaTask(task1);
    }

    @Override
    public Task runTaskTimer(@NotNull Runnable runnable, long delay, long period) {
        ScheduledTask task1;
        switch (schedulerType){
            case ENTITY: task1 = entity.getScheduler().runAtFixedRate(plugin,a-> runnable.run(),retired,delay,period);break;
            case GLOBAL: task1 = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,a-> runnable.run(),delay,period);break;
            case REGION: task1 = Bukkit.getRegionScheduler().runAtFixedRate(plugin,location,a-> runnable.run(),delay,period);break;
            case ASYNC:
            default:task1 = Bukkit.getAsyncScheduler().runAtFixedRate(plugin,a-> runnable.run(),delay*50,period*50,TimeUnit.MILLISECONDS);break;
        }
        return new FoliaTask(task1);
    }

    /**
     * 在Folia中没有Bukkit中"异步"的概念
     * @param runnable 需要执行的程序
     * @return 调度任务实例
     */
    @Override
    public Task runTaskAsynchronously(@NotNull Runnable runnable) {
        return runTask(runnable);
    }

    @Override
    public Task runTaskLaterAsynchronously(@NotNull Runnable runnable, long delay) {
        return runTaskLater(runnable,delay);
    }

    @Override
    public Task runTaskTimerAsynchronously(@NotNull Runnable runnable, long delay, long period) {
        return runTaskTimer(runnable,delay,period);
    }
}
