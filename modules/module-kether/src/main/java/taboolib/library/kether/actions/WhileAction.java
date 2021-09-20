package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.common5.Coerce;
import taboolib.library.kether.*;

import java.util.concurrent.CompletableFuture;

final class WhileAction extends QuestAction<Void> {

    private final ParsedAction<?> condition;
    private final ParsedAction<?> action;

    public WhileAction(ParsedAction<?> condition, ParsedAction<?> action) {
        this.condition = condition;
        this.action = action;
    }

    @Override
    public CompletableFuture<Void> process(@NotNull QuestContext.Frame frame) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        process(frame, future);
        return future;
    }

    private void process(QuestContext.Frame frame, CompletableFuture<Void> future) {
        frame.newFrame(condition).run().thenAcceptAsync(t -> {
            if (Coerce.toBoolean(t)) {
                frame.newFrame(action).run().thenRunAsync(() -> process(frame, future), frame.context().getExecutor());
            } else {
                future.complete(null);
            }
        }, frame.context().getExecutor());
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(resolver -> {
            ParsedAction<?> condition = resolver.nextAction();
            resolver.expect("then");
            ParsedAction<?> action = resolver.nextAction();
            return new WhileAction(condition, action);
        });
    }
}
