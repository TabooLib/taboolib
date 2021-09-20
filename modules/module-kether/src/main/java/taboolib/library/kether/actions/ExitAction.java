package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.library.kether.*;

import java.util.concurrent.CompletableFuture;

final class ExitAction extends QuestAction<Void> {

    private final boolean running;
    private final boolean waiting;
    private final long timeout;

    public ExitAction(boolean running, boolean waiting, long timeout) {
        this.running = running;
        this.waiting = waiting;
        this.timeout = timeout;
    }

    @Override
    public CompletableFuture<Void> process(@NotNull QuestContext.Frame frame) {
        long actual = timeout == 0 ? 0 : System.currentTimeMillis() + timeout;
        frame.context().setExitStatus(new ExitStatus(running, waiting, actual));
        return CompletableFuture.completedFuture(null);
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(reader -> {
            String element = reader.nextToken();
            switch (element) {
                case "success":
                    return new ExitAction(false, false, 0);
                case "pause":
                    return new ExitAction(true, false, 0);
                case "cooldown":
                    long l = reader.next(ArgTypes.DURATION).toMillis();
                    return new ExitAction(false, true, l);
                default:
                    throw LoadError.NOT_MATCH.create("success|pause|cooldown", element);
            }
        });
    }
}
