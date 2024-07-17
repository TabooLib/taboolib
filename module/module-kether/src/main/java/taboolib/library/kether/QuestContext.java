package taboolib.library.kether;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                Stream<Frame> acc = Stream.of(this);
                for (Frame it : children()) {
                    Stream<Frame> frameStream = it.walkFrames(depth - 1);
                    acc = Stream.concat(acc, frameStream);
                }
                return acc;
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

        default <T> T getOrDefault(@NotNull String name, T defaultValue) {
            Optional<T> o = get(name);
            return o.orElse(defaultValue);
        }

        @Nullable
        default <T> T getOrNull(@NotNull String name) {
            Optional<T> o = get(name);
            return o.orElse(null);
        }

        <T> Optional<QuestFuture<T>> getFuture(@NotNull String name);

        void set(@NotNull String name, Object value);

        void remove(@NotNull String name);

        void clear();

        <T> void set(@NotNull String name, @NotNull ParsedAction<T> owner, @NotNull CompletableFuture<T> future);

        Set<String> keys();

        Collection<Map.Entry<String, Object>> values();

        default Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, Object> entry : values()) {
                map.put(entry.getKey(), entry.getValue());
            }
            return map;
        }

        void initialize(@NotNull Frame frame);

        void close();

        VarTable parent();
    }
}
