package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.common5.Coerce;
import taboolib.library.kether.*;

import java.util.concurrent.CompletableFuture;

final class NotAction extends QuestAction<Boolean> {

    private final ParsedAction<?> action;

    public NotAction(ParsedAction<?> action) {
        this.action = action;
    }

    @Override
    public CompletableFuture<Boolean> process(@NotNull QuestContext.Frame frame) {
        return frame.newFrame(action).run().thenApply(t -> !Coerce.toBoolean(t));
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(resolver -> new NotAction(resolver.next(ArgTypes.ACTION)));
    }
}
