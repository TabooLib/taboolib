package taboolib.library.kether;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public interface QuestContext {

    String BASE_BLOCK = "main";

    QuestService<? extends QuestContext> getService();

    Quest getQuest();

    void setExitStatus(ExitStatus exitStatus);

    Optional<ExitStatus> getExitStatus();

    CompletableFuture<Object> runActions();

    Executor getExecutor();

    void terminate();

    Frame rootFrame();

    interface Frame extends AutoCloseable {

        String name();

        QuestContext context();

        Optional<ParsedAction<?>> currentAction();

        List<Frame> children();

        default Stream<Frame> walkFrames() {
            return walkFrames(Integer.MAX_VALUE);
        }

        default Stream<Frame> walkFrames(int depth) {
            if (depth < 0) {
                return Stream.empty();
            } else if (depth == 0) {
                return Stream.of(this);
            } else {
                return this.children().stream().map(it -> it.walkFrames(depth - 1)).reduce(Stream.of(this), Stream::concat);
            }
        }

        Optional<Frame> parent();

        void setNext(@NotNull ParsedAction<?> action);

        void setNext(@NotNull Quest.Block block);

        Frame newFrame(@NotNull String name);

        Frame newFrame(@NotNull ParsedAction<?> action);

        VarTable variables();

        /**
         * The closable will called immediately when action is complete
         *
         * @param closeable resources to clean
         */
        <T extends AutoCloseable> T addClosable(T closeable);

        <T> CompletableFuture<T> run();

        void close();

        boolean isDone();
    }

    interface VarTable {

        <T> Optional<T> get(@NotNull String name) throws CompletionException;

        <T> Optional<QuestFuture<T>> getFuture(@NotNull String name);

        void set(@NotNull String name, Object value);

        void remove(@NotNull String name);

        void clear();

        <T> void set(@NotNull String name, @NotNull ParsedAction<T> owner, @NotNull CompletableFuture<T> future);

        Set<String> keys();

        Collection<Map.Entry<String, Object>> values();

        void initialize(@NotNull Frame frame);

        void close();

        VarTable parent();
    }
}
