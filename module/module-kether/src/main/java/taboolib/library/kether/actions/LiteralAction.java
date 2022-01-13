package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.library.kether.QuestAction;
import taboolib.library.kether.QuestActionParser;
import taboolib.library.kether.QuestContext;

import java.util.concurrent.CompletableFuture;

public class LiteralAction<T> extends QuestAction<T> {

    private final Object value;

    public LiteralAction(Object value) {
        this.value = value;
    }

    public LiteralAction(String value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<T> process(@NotNull QuestContext.Frame frame) {
        return CompletableFuture.completedFuture((T) value);
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(reader -> new LiteralAction<>(reader.nextToken()));
    }
}
