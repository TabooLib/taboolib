package taboolib.library.kether;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

    /** 动作代码不得在持有 frame 锁的情况下被调用，否则父子 frame 之间会形成锁顺序反转 */
    @Test
    void actionIsNotInvokedWhileHoldingFrameLock() {
        AtomicReference<Boolean> heldFrameLock = new AtomicReference<>();
        AtomicReference<Boolean> heldChildLock = new AtomicReference<>();
        TestQuestContext context = context(action(frame -> {
            heldFrameLock.set(Thread.holdsLock(frame));
            QuestContext.Frame child = frame.newFrame(action(inner -> {
                heldChildLock.set(Thread.holdsLock(inner) || Thread.holdsLock(frame));
                return CompletableFuture.completedFuture(null);
            }));
            return child.run();
        }));

        context.runActions().join();

        assertEquals(Boolean.FALSE, heldFrameLock.get());
        assertEquals(Boolean.FALSE, heldChildLock.get());
    }

    /**
     * 终止方向（父 → 子 close）与完成方向（子 resume → 父推进）并发执行时不得死锁。
     * <p>
     * 旧实现两个方向分别持父锁取子锁、持子锁取父锁，构成 ABBA 死锁。
     */
    @Test
    @Timeout(30)
    void concurrentTerminateAndResumeDoesNotDeadlock() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            for (int i = 0; i < 200; i++) {
                CompletableFuture<Object> childAction = new CompletableFuture<>();
                CountDownLatch started = new CountDownLatch(1);
                TestQuestContext context = context(action(frame -> {
                    QuestContext.Frame child = frame.newFrame("child");
                    CompletableFuture<Object> childResult = child.run();
                    started.countDown();
                    return childResult;
                }), action(frame -> CompletableFuture.completedFuture("tail")));
                // child 区块内的动作等待外部 future
                context.quest().putBlock("child", Collections.singletonList(action(frame -> childAction)));

                CompletableFuture<Object> result = context.runActions();
                assertTrue(started.await(5, TimeUnit.SECONDS));

                CountDownLatch fire = new CountDownLatch(1);
                Future<?> terminating = executor.submit(() -> {
                    awaitQuietly(fire);
                    context.terminate();
                });
                Future<?> resuming = executor.submit(() -> {
                    awaitQuietly(fire);
                    childAction.complete("done");
                });
                fire.countDown();

                terminating.get(10, TimeUnit.SECONDS);
                resuming.get(10, TimeUnit.SECONDS);
                assertTrue(result.isDone());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    /** ExitStatus.success() 属于正常结束，以最后一个动作的返回值完成 */
    @Test
    void successExitStatusCompletesNormally() {
        TestQuestContext context = context(action(frame -> {
            frame.context().setExitStatus(ExitStatus.success());
            return CompletableFuture.completedFuture("value");
        }));

        assertEquals("value", context.runActions().join());
    }

    /** ExitStatus.paused() 表示被强制终止，调用方必须能观察到「非正常结束」 */
    @Test
    void pausedExitStatusCancelsResult() {
        AtomicInteger followingRuns = new AtomicInteger();
        TestQuestContext context = context(
            action(frame -> {
                frame.context().setExitStatus(ExitStatus.paused());
                return CompletableFuture.completedFuture("ignored");
            }),
            action(frame -> {
                followingRuns.incrementAndGet();
                return CompletableFuture.completedFuture(null);
            })
        );

        CompletableFuture<Object> result = context.runActions();

        assertTrue(result.isCancelled());
        assertEquals(0, followingRuns.get());
    }

    /** ExitStatus.cooldown() 同样是「仍在运行」的挂起态，不应被当作成功 */
    @Test
    void cooldownExitStatusCancelsResult() {
        TestQuestContext context = context(action(frame -> {
            frame.context().setExitStatus(ExitStatus.cooldown(1000));
            return CompletableFuture.completedFuture("ignored");
        }));

        assertTrue(context.runActions().isCancelled());
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

        TestQuest quest() {
            return (TestQuest) getQuest();
        }

        @Override
        protected Executor createExecutor() {
            return Runnable::run;
        }
    }

    private static class TestQuest implements Quest {

        private final Map<String, Block> blocks = new ConcurrentHashMap<>();

        TestQuest(List<ParsedAction<?>> actions) {
            this.blocks.put(QuestContext.BASE_BLOCK, new TestBlock(QuestContext.BASE_BLOCK, actions));
        }

        /** 追加命名区块，供嵌套 frame 测试使用 */
        void putBlock(String label, List<ParsedAction<?>> actions) {
            this.blocks.put(label, new TestBlock(label, actions));
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
            return Collections.unmodifiableMap(blocks);
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
