package taboolib.expansion.folialib.Wrapper.SchedulerWrapper;

import taboolib.expansion.folialib.Wrapper.Scheduler;
import taboolib.expansion.folialib.Wrapper.Task;;
import taboolib.expansion.folialib.Wrapper.TaskWrapper.BukkitTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;


public class BukkitScheduler implements Scheduler {
    private final Plugin plugin;

    public BukkitScheduler(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public Task runTask(@NotNull Runnable runnable) {
        return new BukkitTask(Bukkit.getScheduler().runTask(plugin,runnable),false);
    }

    @Override
    public Task runTaskLater(@NotNull Runnable runnable, long delay) {
        return new BukkitTask(Bukkit.getScheduler().runTaskLater(plugin,runnable,delay),false);
    }

    @Override
    public Task runTaskTimer(@NotNull Runnable runnable, long delay, long period) {
        return new BukkitTask(Bukkit.getScheduler().runTaskTimer(plugin,runnable,delay,period),true);
    }

    @Override
    public Task runTaskAsynchronously(@NotNull Runnable runnable) {
        return new BukkitTask(Bukkit.getScheduler().runTaskAsynchronously(plugin,runnable),false);
    }

    @Override
    public Task runTaskLaterAsynchronously(@NotNull Runnable runnable, long delay) {
        return new BukkitTask(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,runnable,delay),false);
    }

    @Override
    public Task runTaskTimerAsynchronously(@NotNull Runnable runnable, long delay, long period) {
        return new BukkitTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,runnable,delay,period),true);
    }
}
