package taboolib.expansion.folialib.Wrapper;

import org.bukkit.plugin.Plugin;

public interface Task {
    Plugin getOwningPlugin();
    void cancel();
    boolean isCancelled();
    boolean isTimerTask();
    boolean isAsyncTask();
}
