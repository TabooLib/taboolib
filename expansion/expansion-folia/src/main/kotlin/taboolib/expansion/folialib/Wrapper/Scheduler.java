package taboolib.expansion.folialib.Wrapper;

import org.jetbrains.annotations.NotNull;


public interface Scheduler {
    Task runTask(@NotNull Runnable runnable);

    Task runTaskLater(@NotNull Runnable runnable, long delay);

    Task runTaskTimer(@NotNull Runnable runnable, long delay, long period);

    Task runTaskAsynchronously(@NotNull Runnable runnable);

    Task runTaskLaterAsynchronously(@NotNull Runnable runnable, long delay);

    Task runTaskTimerAsynchronously(@NotNull Runnable runnable, long delay, long period);
}
