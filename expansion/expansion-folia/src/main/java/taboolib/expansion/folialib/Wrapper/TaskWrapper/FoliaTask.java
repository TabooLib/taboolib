package taboolib.expansion.folialib.Wrapper.TaskWrapper;

import taboolib.expansion.folialib.Wrapper.Task;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;

public class FoliaTask implements Task {
    private final ScheduledTask task;
    public FoliaTask(ScheduledTask task){
        this.task = task;
    }
    @Override
    public Plugin getOwningPlugin() {
        return task.getOwningPlugin();
    }

    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }

    @Override
    public boolean isTimerTask() {
        return task.isRepeatingTask();
    }

    @Override
    public boolean isAsyncTask() {
        return true;
    }
}
