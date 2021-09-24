package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.common5.Coerce;
import taboolib.library.kether.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class AnyAction extends QuestAction<Boolean> {

    private final List<ParsedAction<?>> actions;

    public AnyAction(List<ParsedAction<?>> actions) {
        this.actions = actions;
    }

    @Override
    public CompletableFuture<Boolean> process(@NotNull QuestContext.Frame frame) {
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(false);
        for (ParsedAction<?> action : actions) {
            CompletableFuture<?> f = frame.newFrame(action).run();
            future = future.thenCombine(f, (b, o) -> b || Coerce.toBoolean(o));
        }
        return future;
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(resolver -> new AnyAction(resolver.next(ArgTypes.listOf(ArgTypes.ACTION))));
    }
}
