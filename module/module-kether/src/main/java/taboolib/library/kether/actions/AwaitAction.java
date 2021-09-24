package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.library.kether.*;

import java.util.concurrent.CompletableFuture;

final class AwaitAction<T> extends QuestAction<T> {

    private final ParsedAction<T> action;

    public AwaitAction(ParsedAction<T> action) {
        this.action = action;
    }

    @Override
    public CompletableFuture<T> process(@NotNull QuestContext.Frame frame) {
        CompletableFuture<T> future = new CompletableFuture<>();
        frame.newFrame(action).<T>run().thenAccept(QuestFuture.complete(future));
        return future;
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(resolver -> new AwaitAction<>(resolver.nextAction()));
    }
}
