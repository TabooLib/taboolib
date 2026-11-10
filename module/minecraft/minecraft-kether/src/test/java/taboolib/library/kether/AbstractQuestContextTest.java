package taboolib.library.kether;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractQuestContextTest {

    @Test
    void asynchronousActionFailureCompletesContextExceptionally() {
        CompletableFuture<Object> actionFuture = new CompletableFuture<>();
        IllegalStateException failure = new IllegalStateException("boom");
        TestQuestContext context = context(action(frame -> actionFuture));

        CompletableFuture<Object> result = context.runActions();
        actionFuture.completeExceptionally(failure);

        CompletionException thrown = assertThrows(CompletionException.class, result::join);
        assertSame(failure, thrown.getCause());
    }

    @Test
    void completedExceptionalActionDoesNotEscapeRunActions() {
        IllegalStateException failure = new IllegalStateException("boom");
        CompletableFuture<Object> failed = new CompletableFuture<>();
        failed.completeExceptionally(failure);
        TestQuestContext context = context(action(frame -> failed));

        CompletableFuture<Object> result = assertDoesNotThrow(context::runActions);

        CompletionException thrown = assertThrows(CompletionException.class, result::join);
        assertSame(failure, thrown.getCause());
    }

    @Test
    void synchronousActionFailureStopsFollowingActions() {
        IllegalStateException failure = new IllegalStateException("boom");
        AtomicInteger followingRuns = new AtomicInteger();
        TestQuestContext context = context(
            action(frame -> {
                throw failure;
            }),
            action(frame -> {
                followingRuns.incrementAndGet();
                return CompletableFuture.completedFuture(null);
            })
        );

        CompletableFuture<Object> result = assertDoesNotThrow(context::runActions);

        CompletionException thrown = assertThrows(CompletionException.class, result::join);
        assertSame(failure, thrown.getCause());
        assertEquals(0, followingRuns.get());
    }

    @Test
    void actionFrameConvertsSynchronousFailureToFuture() {
        IllegalStateException failure = new IllegalStateException("boom");
        TestQuestContext context = context();
        QuestContext.Frame frame = context.rootFrame().newFrame(action(ignored -> {
            throw failure;
        }));

        CompletableFuture<Object> result = assertDoesNotThrow(() -> {
            return frame.run();
        });

        CompletionException thrown = assertThrows(CompletionException.class, result::join);
        assertSame(failure, thrown.getCause());
    }

    @Test
    void exitStatusCompletesWithLastActionValue() {
        AtomicInteger closes = new AtomicInteger();
        TestQuestContext context = context(action(frame -> {
            frame.addClosable(closes::incrementAndGet);
            frame.context().setExitStatus(ExitStatus.success());
            return CompletableFuture.completedFuture(7);
        }));

        assertEquals(7, context.runActions().join());
        assertEquals(1, closes.get());
    }

    @Test
    void cancellingContextCancelsRunningAction() {
        CompletableFuture<Object> actionFuture = new CompletableFuture<>();
        TestQuestContext context = context(action(frame -> actionFuture));
        CompletableFuture<Object> result = context.runActions();

        assertTrue(result.cancel(false));

        assertTrue(actionFuture.isCancelled());
        assertTrue(result.isCancelled());
    }

    @Test
    void terminatingContextClosesFrameAndRunningAction() {
        CompletableFuture<Object> actionFuture = new CompletableFuture<>();
        TestQuestContext context = context(action(frame -> actionFuture));
        CompletableFuture<Object> result = context.runActions();

        context.terminate();

        assertTrue(actionFuture.isCancelled());
        assertTrue(result.isCompletedExceptionally());
        assertFalse(result.isCancelled());
    }

    @SafeVarargs
    private final TestQuestContext context(ParsedAction<?>... actions) {
        return new TestQuestContext(new TestQuest(Arrays.asList(actions)));
    }

    private ParsedAction<Object> action(ActionProcessor processor) {
        return new ParsedAction<>(new QuestAction<Object>() {
            @Override
            public CompletableFuture<Object> process(@NotNull QuestContext.Frame frame) {
                return processor.process(frame);
            }
        });
    }

    private interface ActionProcessor {

        CompletableFuture<Object> process(QuestContext.Frame frame);
    }

    private static class TestQuestContext extends AbstractQuestContext<TestQuestContext> {

        TestQuestContext(Quest quest) {
            super(null, quest, "test");
        }

        @Override
        protected Executor createExecutor() {
            return Runnable::run;
        }
    }

    private static class TestQuest implements Quest {

        private final Map<String, Block> blocks;

        TestQuest(List<ParsedAction<?>> actions) {
            Map<String, Block> values = new LinkedHashMap<>();
            values.put(QuestContext.BASE_BLOCK, new TestBlock(QuestContext.BASE_BLOCK, actions));
            this.blocks = Collections.unmodifiableMap(values);
        }

        @Override
        public String getId() {
            return "test";
        }

        @Override
        public Optional<Block> getBlock(@NotNull String label) {
            return Optional.ofNullable(blocks.get(label));
        }

        @Override
        public Map<String, Block> getBlocks() {
            return blocks;
        }

        @Override
        public Optional<Block> blockOf(@NotNull ParsedAction<?> action) {
            return blocks.values().stream().filter(block -> block.indexOf(action) >= 0).findFirst();
        }
    }

    private static class TestBlock implements Quest.Block {

        private final String label;
        private final List<ParsedAction<?>> actions;

        TestBlock(String label, List<ParsedAction<?>> actions) {
            this.label = label;
            this.actions = actions;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public List<ParsedAction<?>> getActions() {
            return actions;
        }

        @Override
        public int indexOf(@NotNull ParsedAction<?> action) {
            return actions.indexOf(action);
        }

        @Override
        public Optional<ParsedAction<?>> get(int index) {
            return index >= 0 && index < actions.size() ? Optional.of(actions.get(index)) : Optional.empty();
        }
    }
}
