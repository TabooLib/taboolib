package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.common5.Coerce;
import taboolib.library.kether.*;

import java.util.concurrent.CompletableFuture;

final class IfAction<U> extends QuestAction<U> {

    private final ParsedAction<?> condition;
    private final ParsedAction<U> trueAction;
    private final ParsedAction<U> falseAction;

    public IfAction(ParsedAction<?> condition, ParsedAction<U> trueAction, ParsedAction<U> falseAction) {
        this.condition = condition;
        this.trueAction = trueAction;
        this.falseAction = falseAction;
    }

    @Override
    public CompletableFuture<U> process(@NotNull QuestContext.Frame frame) {
        CompletableFuture<U> future = new CompletableFuture<>();
        frame.newFrame(condition).run().thenAccept(t -> {
            if (Coerce.toBoolean(t)) {
                frame.newFrame(trueAction).<U>run().thenAccept(future::complete);
            } else {
                frame.newFrame(falseAction).<U>run().thenAccept(future::complete);
            }
        });
        return future;
    }

    public static <U, CTX extends QuestContext> QuestActionParser parser(QuestService<CTX> service) {
        return QuestActionParser.of(resolver -> {
            ParsedAction<?> condition = resolver.nextAction();
            resolver.expect("then");
            ParsedAction<U> trueAction = resolver.nextAction();
            if (resolver.hasNext()) {
                resolver.mark();
                String element = resolver.nextToken();
                if (element.equals("else")) {
                    ParsedAction<U> falseAction = resolver.nextAction();
                    return new IfAction<>(condition, trueAction, falseAction);
                } else {
                    resolver.reset();
                }
            }
            return new IfAction<>(condition, trueAction, ParsedAction.noop());
        });
    }
}
