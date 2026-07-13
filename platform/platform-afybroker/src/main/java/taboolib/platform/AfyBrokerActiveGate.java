package taboolib.platform;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

final class AfyBrokerActiveGate {

    private enum State {
        OPEN,
        ACTIVATING,
        CLOSED
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.OPEN);
    private final CompletableFuture<Void> activationClosed = new CompletableFuture<>();

    boolean activate(Runnable action) {
        if (!state.compareAndSet(State.OPEN, State.ACTIVATING)) {
            return false;
        }
        try {
            action.run();
            return true;
        } finally {
            state.set(State.CLOSED);
            activationClosed.complete(null);
        }
    }

    CompletableFuture<Void> close() {
        if (state.compareAndSet(State.OPEN, State.CLOSED)) {
            activationClosed.complete(null);
        }
        return activationClosed;
    }
}
