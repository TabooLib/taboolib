package taboolib.platform;

import taboolib.common.LifeCycle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

final class AppLifeCycle {

    private static final List<LifeCycle> INITIALIZATION = Collections.unmodifiableList(Arrays.asList(
            LifeCycle.CONST,
            LifeCycle.INIT,
            LifeCycle.LOAD,
            LifeCycle.ENABLE,
            LifeCycle.ACTIVE
    ));

    private enum State {
        NEW,
        INITIALIZING,
        ACTIVE,
        STOP_REQUESTED,
        DISABLING,
        DISABLED
    }

    private final Object lock = new Object();
    private State state = State.NEW;
    private boolean transitionRunning;

    static List<LifeCycle> initialization() {
        return INITIALIZATION;
    }

    boolean run(Consumer<LifeCycle> action) {
        synchronized (lock) {
            if (state == State.INITIALIZING || state == State.ACTIVE) {
                return true;
            }
            if (state != State.NEW) {
                return false;
            }
            state = State.INITIALIZING;
        }
        for (LifeCycle lifeCycle : INITIALIZATION) {
            if (!beginTransition()) {
                return isRunning();
            }
            Throwable failure = null;
            try {
                action.accept(lifeCycle);
            } catch (Throwable ex) {
                failure = ex;
            }
            boolean disable = finishTransition();
            if (disable) {
                try {
                    runDisable(action);
                } catch (Throwable ex) {
                    if (failure == null) {
                        failure = ex;
                    } else {
                        failure.addSuppressed(ex);
                    }
                }
            }
            if (failure != null) {
                AppLifeCycle.<RuntimeException>rethrow(failure);
            }
            if (disable) {
                return false;
            }
        }
        synchronized (lock) {
            if (state == State.INITIALIZING) {
                state = State.ACTIVE;
                return true;
            }
            return state == State.ACTIVE;
        }
    }

    void shutdown(Consumer<LifeCycle> action) {
        boolean disable = false;
        synchronized (lock) {
            switch (state) {
                case NEW:
                case ACTIVE:
                    state = State.DISABLING;
                    disable = true;
                    break;
                case INITIALIZING:
                    state = State.STOP_REQUESTED;
                    if (!transitionRunning) {
                        state = State.DISABLING;
                        disable = true;
                    }
                    break;
                case STOP_REQUESTED:
                case DISABLING:
                case DISABLED:
                    return;
            }
        }
        if (disable) {
            runDisable(action);
        }
    }

    private boolean beginTransition() {
        synchronized (lock) {
            if (state != State.INITIALIZING) {
                return false;
            }
            transitionRunning = true;
            return true;
        }
    }

    private boolean finishTransition() {
        synchronized (lock) {
            transitionRunning = false;
            if (state == State.STOP_REQUESTED) {
                state = State.DISABLING;
                return true;
            }
            return false;
        }
    }

    private void runDisable(Consumer<LifeCycle> action) {
        try {
            action.accept(LifeCycle.DISABLE);
        } finally {
            synchronized (lock) {
                transitionRunning = false;
                state = State.DISABLED;
            }
        }
    }

    private boolean isRunning() {
        synchronized (lock) {
            return state == State.INITIALIZING || state == State.ACTIVE;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void rethrow(Throwable throwable) throws T {
        throw (T) throwable;
    }
}
