package io.izzel.taboolib.module.event;

import org.bukkit.event.Cancellable;

/**
 * 可取消类监听器简化接口
 *
 * @author sky
 * @since 2019-10-22 10:41
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class EventCancellable<T extends EventCancellable> extends EventNormal implements Cancellable {

    private boolean cancelled;

    public T nonCancelled(Runnable runnable) {
        if (nonCancelled()) {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return (T) this;
    }

    public T ifCancelled(Runnable runnable) {
        if (isCancelled()) {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return (T) this;
    }

    @Override
    public T call() {
        return (T) super.call();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public boolean nonCancelled() {
        return !cancelled;
    }
}
