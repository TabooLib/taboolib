package taboolib.library.kether;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class QuestAction<T> {

    /**
     * This should not be called directly, see {@link QuestContext.Frame#newFrame(ParsedAction)}
     */
    public abstract CompletableFuture<T> process(@NotNull QuestContext.Frame frame);

    public static <T> QuestAction<T> noop() {
        return new QuestAction<T>() {

            @Override
            public CompletableFuture<T> process(@NotNull QuestContext.Frame frame) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public String toString() {
                return "NoOp{}";
            }
        };
    }
}
