package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.library.kether.*;

import java.util.concurrent.CompletableFuture;

public class AsyncAction<T> extends QuestAction<QuestFuture<T>> {

    private final ParsedAction<T> action;

    public AsyncAction(ParsedAction<T> action) {
        this.action = action;
    }

    @Override
    public CompletableFuture<QuestFuture<T>> process(@NotNull QuestContext.Frame frame) {
        CompletableFuture<T> future = frame.newFrame(action).run();
        return CompletableFuture.completedFuture(new QuestFuture<>(action, future));
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(resolver -> new AsyncAction<>(resolver.nextAction()));
    }
}
