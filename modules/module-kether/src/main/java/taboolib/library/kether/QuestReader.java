package taboolib.library.kether;

import org.jetbrains.annotations.NotNull;

public interface QuestReader {

    char peek();

    char peek(int n);

    int getIndex();

    int getMark();

    boolean hasNext();

    String nextToken();

    void mark();

    void reset();

    <T> ParsedAction<T> nextAction();

    void expect(@NotNull String value);

    default int nextInt() {
        return next(ArgTypes.INT);
    }

    default long nextLong() {
        return next(ArgTypes.LONG);
    }

    default double nextDouble() {
        return next(ArgTypes.DOUBLE);
    }

    default <T> T next(@NotNull ArgType<T> argType) throws LocalizedException {
        return argType.read(this);
    }
}
