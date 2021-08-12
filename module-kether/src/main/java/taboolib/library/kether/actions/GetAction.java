package taboolib.library.kether.actions;

import org.jetbrains.annotations.NotNull;
import taboolib.library.kether.QuestAction;
import taboolib.library.kether.QuestActionParser;
import taboolib.library.kether.QuestContext;

import java.util.concurrent.CompletableFuture;

public class GetAction<T> extends QuestAction<T> {

    private final String key;

    public GetAction(String key) {
        this.key = key;
    }

    @Override
    public CompletableFuture<T> process(@NotNull QuestContext.Frame frame) {
        return CompletableFuture.completedFuture(frame.variables().<T>get(key).orElse(null));
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(resolver -> new GetAction<>(resolver.nextToken()));
    }
}
