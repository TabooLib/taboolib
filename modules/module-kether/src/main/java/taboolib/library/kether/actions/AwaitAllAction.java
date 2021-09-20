package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.library.kether.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class AwaitAllAction extends QuestAction<Void> {

    private final List<ParsedAction<?>> actions;

    public AwaitAllAction(List<ParsedAction<?>> actions) {
        this.actions = actions;
    }

    @Override
    public CompletableFuture<Void> process(@NotNull QuestContext.Frame frame) {
        CompletableFuture<?>[] futures = new CompletableFuture[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            ParsedAction<?> action = actions.get(i);
            futures[i] = frame.newFrame(action).run();
        }
        return CompletableFuture.allOf(futures);
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(resolver -> new AwaitAllAction(resolver.next(ArgTypes.listOf(ArgTypes.ACTION))));
    }
}
