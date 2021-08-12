package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.common5.Coerce;
import taboolib.library.kether.*;

import java.util.concurrent.CompletableFuture;

final class RepeatAction extends QuestAction<Void> {

    private final int time;
    private final ParsedAction<?> action;

    public RepeatAction(int time, ParsedAction<?> action) {
        this.time = time;
        this.action = action;
    }

    @Override
    public CompletableFuture<Void> process(@NotNull QuestContext.Frame frame) {
        int cur = Coerce.toInteger(frame.variables().get("times").orElse(0));
        CompletableFuture<Void> future = new CompletableFuture<>();
        process(frame, future, cur);
        return future;
    }

    private void process(QuestContext.Frame frame, CompletableFuture<Void> future, int cur) {
        if (cur < time) {
            frame.newFrame(action).run().thenRunAsync(() -> {
                frame.variables().set("times", cur + 1);
                process(frame, future, cur + 1);
            }, frame.context().getExecutor());
        } else {
            frame.variables().set("times", null);
            future.complete(null);
        }
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(resolver -> new RepeatAction(resolver.nextInt(), resolver.nextAction()));
    }
}
