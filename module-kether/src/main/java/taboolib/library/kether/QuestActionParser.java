package taboolib.library.kether;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface QuestActionParser {

    <T> QuestAction<T> resolve(@NotNull QuestReader resolver);

    @SuppressWarnings("unchecked")
    static <T> QuestActionParser of(Function<QuestReader, QuestAction<T>> resolveFunction) {
        return new QuestActionParser() {

            @Override
            public <AT> QuestAction<AT> resolve(@NotNull QuestReader resolver) {
                return (QuestAction<AT>) resolveFunction.apply(resolver);
            }
        };
    }
}
