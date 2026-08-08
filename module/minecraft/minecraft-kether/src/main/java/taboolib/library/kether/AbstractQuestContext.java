package taboolib.library.kether;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractQuestContext<T extends AbstractQuestContext<T>> implements QuestContext {

    protected final QuestService<T> service;
    protected final Frame rootFrame;
    protected final Quest quest;
    protected final QuestExecutor executor;
    protected volatile ExitStatus exitStatus;
    protected volatile CompletableFuture<Object> future;

    /** 运行标记，保证同一上下文不会被并发启动（替代 synchronized，避免持锁调用动作代码） */
    private final AtomicBoolean running = new AtomicBoolean();

    protected AbstractQuestContext(QuestService<T> service, Quest quest, String playerIdentifier) {
        this.service = service;
        this.quest = quest;
        this.rootFrame = createRootFrame();
        this.executor = new QuestExecutor(this);
    }

    protected abstract Executor createExecutor();

    protected Frame createRootFrame() {
        return new SimpleNamedFrame(null, newFrameList(), new SimpleVarTable(null), QuestContext.BASE_BLOCK, this);
    }

    /**
     * 创建子 frame 容器。
     * <p>
     * 使用同步列表而非 {@link LinkedList}：frame 的推进循环（{@code removeIf}）与关闭路径（遍历）
     * 可能位于不同线程，需要保证列表自身的结构安全。该列表锁只用于纯粹的列表操作，
     * 不会在持有期间回调外部代码，因此是不会参与死锁的叶子锁。
     */
    static List<Frame> newFrameList() {
        return Collections.synchronizedList(new ArrayList<>());
    }

    public QuestService<T> getService() {
        return service;
    }

    @Override
    public Quest getQuest() {
        return quest;
    }

    @Override
    public void setExitStatus(ExitStatus exitStatus) {
        this.exitStatus = exitStatus;
    }

    @Override
    public Optional<ExitStatus> getExitStatus() {
        return Optional.ofNullable(exitStatus);
    }

    @Override
    public QuestExecutor getExecutor() {
        return executor;
    }

    @Override
    public Frame rootFrame() {
        return rootFrame;
    }

    @Override
    public CompletableFuture<Object> runActions() {
        Preconditions.checkState(running.compareAndSet(false, true), "already running");
        CompletableFuture<Object> frameFuture = rootFrame.run();
        CompletableFuture<Object> contextFuture = new CompletableFuture<>();
        this.future = contextFuture;
        frameFuture.whenComplete((result, ex) -> {
            if (ex != null) {
                completeFailure(contextFuture, ex);
            } else {
                if (this.exitStatus == null) {
                    this.exitStatus = ExitStatus.success();
                }
                contextFuture.complete(result);
            }
        });
        contextFuture.whenComplete((result, ex) -> {
            if (contextFuture.isCancelled()) {
                frameFuture.cancel(false);
            }
        });
        return contextFuture;
    }

    @Override
    public void terminate() {
        // 不加锁：close() 内部会完成 future 并回调外部代码，持锁调用外部回调会引入死锁风险
        this.rootFrame.close();
        CompletableFuture<Object> contextFuture = this.future;
        this.future = null;
        this.running.set(false);
        if (contextFuture != null) {
            contextFuture.completeExceptionally(new QuestCloseException());
        }
    }

    private static void completeFailure(CompletableFuture<?> future, Throwable throwable) {
        Throwable cause = throwable;
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof CancellationException) {
            future.cancel(false);
        } else {
            future.completeExceptionally(cause);
        }
    }

    public static class QuestExecutor implements Executor {

        private final AbstractQuestContext<?> questContext;
        private final Executor actual;

        public QuestExecutor(AbstractQuestContext<?> questContext) {
            this.questContext = questContext;
            this.actual = questContext.createExecutor();
        }

        @Override
        public void execute(@NotNull Runnable command) {
            if (!questContext.getExitStatus().isPresent()) {
                actual.execute(command);
            }
        }
    }

    public static abstract class AbstractFrame implements Frame {

        protected final Frame parent;
        protected final List<Frame> frames;
        protected final VarTable varTable;
        protected final QuestContext questContext;
        protected final AtomicReference<CompletableFuture<?>> futureRef = new AtomicReference<>();
        protected final Deque<AutoCloseable> closeables = new LinkedBlockingDeque<>();

        public AbstractFrame(Frame parent, List<Frame> frames, VarTable varTable, QuestContext questContext) {
            this.parent = parent;
            this.frames = frames;
            this.varTable = varTable;
            this.questContext = questContext;
        }

        @Override
        public QuestContext context() {
            return questContext;
        }

        @Override
        public List<Frame> children() {
            return this.frames;
        }

        @Override
        public Optional<Frame> parent() {
            return Optional.ofNullable(parent);
        }

        @Override
        public Frame newFrame(@NotNull String name) {
            SimpleNamedFrame frame = new SimpleNamedFrame(this, newFrameList(), new SimpleVarTable(this), name, context());
            this.frames.add(frame);
            return frame;
        }

        @Override
        public Frame newFrame(@NotNull ParsedAction<?> action) {
            Frame frame;
            if (action.get(ActionProperties.REQUIRE_FRAME, false)) {
                frame = new SimpleNamedFrame(this, newFrameList(), new SimpleVarTable(this), "__anon__" + System.nanoTime(), context());
                frame.setNext(action);
            } else {
                frame = new SimpleActionFrame(this, newFrameList(), new SimpleVarTable(this), action, context());
            }
            this.frames.add(frame);
            return frame;
        }

        @Override
        public VarTable variables() {
            return this.varTable;
        }

        @Override
        public <T extends AutoCloseable> T addClosable(T closeable) {
            this.closeables.addFirst(closeable);
            return closeable;
        }

        /**
         * 关闭当前 frame 及其所有子 frame。
         * <p>
         * 全程不持有任何 frame 锁：子 frame 的关闭、future 的完成回调都可能触发外部代码
         * （动作注册的 whenComplete、父 frame 的推进），持锁执行会造成父子锁顺序反转。
         * 通过 {@link #futureRef} 的 CAS 保证同一个 frame 只会真正关闭一次。
         */
        @Override
        public void close() {
            CompletableFuture<?> runningFuture = this.futureRef.getAndSet(null);
            if (runningFuture == null) return;
            // 快照子列表后再遍历，避免关闭过程中子 frame 完成导致的并发修改
            for (Frame frame : snapshotFrames()) {
                frame.close();
            }
            this.cleanup();
            runningFuture.completeExceptionally(new QuestCloseException());
        }

        /** 对子 frame 列表做快照，避免遍历期间的并发修改 */
        protected List<Frame> snapshotFrames() {
            synchronized (this.frames) {
                return new ArrayList<>(this.frames);
            }
        }

        /** 移除所有已完成的子 frame */
        protected void removeDoneFrames() {
            synchronized (this.frames) {
                this.frames.removeIf(Frame::isDone);
            }
        }

        @Override
        public boolean isDone() {
            CompletableFuture<?> runningFuture = this.futureRef.get();
            return runningFuture == null || runningFuture.isDone();
        }

        void cleanup() {
            while (!closeables.isEmpty()) {
                try {
                    closeables.pollFirst().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class SimpleNamedFrame extends AbstractFrame {

        private final String name;
        private Quest.Block block, next;
        private int sp = -1, np = -1;
        private final AtomicReference<CompletableFuture<?>> runningAction = new AtomicReference<>();

        /**
         * 推进循环的所有权令牌。
         * <p>
         * 0 = 空闲；>0 = 已有线程在推进（数值表示待处理的推进请求数）。
         * 由 CAS 保证同一时刻只有一个线程在跑 {@link #process}，其余线程只登记请求后立即返回，
         * 由持有令牌的线程代为执行——即所谓"排水循环"（drain loop）。
         * 这样既保证了动作按序串行执行，又完全不需要在调用 {@code action.process(...)} 时持锁。
         */
        private final AtomicInteger pending = new AtomicInteger();

        /** 待推进时携带的上一个动作 future（用于取回最终返回值） */
        private final AtomicReference<CompletableFuture<?>> pendingPrevious = new AtomicReference<>();

        public SimpleNamedFrame(Frame parent, List<Frame> frames, VarTable varTable, String name, QuestContext questContext) {
            super(parent, frames, varTable, questContext);
            this.name = name;
            context().getQuest().getBlock(name).ifPresent(this::setNext);
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public Optional<ParsedAction<?>> currentAction() {
            if (block == null || sp == -1) {
                return Optional.empty();
            } else {
                return block.get(sp);
            }
        }

        @Override
        public void setNext(@NotNull ParsedAction<?> action) {
            if (block != null) {
                np = block.indexOf(action);
                if (np == -1) next = null;
            }
            if (next == null) {
                Optional<Quest.Block> optional = context().getQuest().blockOf(action);
                if (optional.isPresent()) {
                    next = optional.get();
                    np = next.indexOf(action);
                } else {
                    throw new IllegalArgumentException(action + " is not in quest");
                }
            }
        }

        @Override
        public void setNext(@NotNull Quest.Block block) {
            next = block;
            np = 0;
        }

        /**
         * 关闭 frame，同时取消仍在运行的动作 future。
         * <p>
         * 不加锁：{@code super.close()} 会级联关闭子 frame 并完成 future，这两步都会回调外部代码。
         */
        @Override
        public void close() {
            CompletableFuture<?> actionFuture = this.runningAction.getAndSet(null);
            super.close();
            if (actionFuture != null) {
                actionFuture.cancel(false);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> CompletableFuture<T> run() {
            CompletableFuture<Object> resultFuture = new CompletableFuture<>();
            Preconditions.checkState(this.futureRef.compareAndSet(null, resultFuture), "already running");
            try {
                varTable.initialize(this);
            } catch (Throwable ex) {
                // 变量初始化失败时归还运行权，避免 frame 永久停留在"运行中"
                this.futureRef.compareAndSet(resultFuture, null);
                throw ex;
            }
            resultFuture.whenComplete((result, ex) -> {
                if (resultFuture.isCancelled()) {
                    this.close();
                }
            });
            schedule(resultFuture, null);
            return (CompletableFuture<T>) resultFuture;
        }

        /**
         * 登记一次推进请求，并在成为令牌持有者时执行排水循环。
         * <p>
         * 若已有线程在推进（例如动作在 {@code process} 调用栈内同步完成并回调 resume），
         * 本次调用只增加计数后立即返回，由那个线程继续处理——从而消除递归与嵌套加锁。
         */
        private void schedule(CompletableFuture<?> resultFuture, CompletableFuture<?> previousFuture) {
            this.pendingPrevious.set(previousFuture);
            if (this.pending.getAndIncrement() != 0) {
                // 已有线程持有令牌，交由它继续推进
                return;
            }
            do {
                process(resultFuture, this.pendingPrevious.get());
            } while (this.pending.decrementAndGet() != 0);
        }

        /**
         * 推进动作序列。调用方保证同一时刻只有一个线程进入本方法。
         * <p>
         * 方法内部**不持有任何锁**，因此 {@code action.process(this)} 这类外部回调可以安全地
         * 创建子 frame、同步完成 future 甚至反向触发父 frame 的推进。
         */
        private void process(CompletableFuture<?> resultFuture, CompletableFuture<?> previousFuture) {
            if (resultFuture.isDone() || this.futureRef.get() != resultFuture) {
                return;
            }
            while (true) {
                Optional<ExitStatus> status = context().getExitStatus();
                if (status.isPresent()) {
                    // 上下文已给出终态：按 isRunning() 区分「正常结束」与「被中断」
                    this.cleanup();
                    removeDoneFrames();
                    completeByExitStatus(resultFuture, previousFuture, status.get());
                    return;
                }
                this.cleanup();
                removeDoneFrames();
                Optional<? extends ParsedAction<?>> optional = nextAction();
                if (!optional.isPresent()) {
                    completeResult(resultFuture, previousFuture);
                    return;
                }
                ParsedAction<?> action = optional.get();
                CompletableFuture<?> actionFuture;
                try {
                    actionFuture = Objects.requireNonNull(action.process(this), "Quest action returned null future: " + action);
                } catch (Throwable ex) {
                    fail(resultFuture, ex);
                    return;
                }
                if (!actionFuture.isDone()) {
                    this.runningAction.set(actionFuture);
                    // 二次确认：登记期间 frame 可能已被 close()，此时需要主动取消，避免动作泄漏
                    if (this.futureRef.get() != resultFuture || resultFuture.isDone()) {
                        this.runningAction.compareAndSet(actionFuture, null);
                        actionFuture.cancel(false);
                        return;
                    }
                    actionFuture.whenComplete((result, ex) -> resume(resultFuture, actionFuture, ex));
                    return;
                }
                if (actionFuture.isCancelled()) {
                    resultFuture.cancel(false);
                    return;
                }
                try {
                    actionFuture.join();
                } catch (Throwable ex) {
                    fail(resultFuture, ex);
                    return;
                }
                previousFuture = actionFuture;
            }
        }

        /** 异步动作完成后的回调入口，只登记推进请求，实际推进交由排水循环执行 */
        private void resume(CompletableFuture<?> resultFuture, CompletableFuture<?> actionFuture, Throwable throwable) {
            this.runningAction.compareAndSet(actionFuture, null);
            if (this.futureRef.get() != resultFuture || resultFuture.isDone()) {
                return;
            }
            if (throwable != null) {
                fail(resultFuture, throwable);
            } else {
                schedule(resultFuture, actionFuture);
            }
        }

        private void fail(CompletableFuture<?> resultFuture, Throwable throwable) {
            this.cleanup();
            removeDoneFrames();
            completeFailure(resultFuture, throwable);
        }

        /**
         * 按 {@link ExitStatus} 语义完成 future。
         * <p>
         * {@code success()} 为正常结束，用最后一个动作的返回值完成；
         * {@code paused()} / {@code cooldown(..)} 均满足 {@code isRunning() == true}，
         * 表示脚本是被外部强制中断（如 {@code Workspace.terminateScript}）而非跑完，
         * 此时走 {@code cancel(false)}，让调用方能够区分「被中断」与「成功」。
         */
        private void completeByExitStatus(CompletableFuture<?> resultFuture, CompletableFuture<?> previousFuture, ExitStatus status) {
            if (status.isRunning()) {
                resultFuture.cancel(false);
            } else {
                completeResult(resultFuture, previousFuture);
            }
        }

        @SuppressWarnings("unchecked")
        private void completeResult(CompletableFuture<?> resultFuture, CompletableFuture<?> previousFuture) {
            Object result = previousFuture != null ? previousFuture.getNow(null) : null;
            ((CompletableFuture<Object>) resultFuture).complete(result);
        }

        private Optional<? extends ParsedAction<?>> nextAction() {
            if (next != null && np != -1) {
                return (block = next).get(sp = np++);
            } else return Optional.empty();
        }
    }

    public static class SimpleActionFrame extends AbstractFrame {

        protected final ParsedAction<?> action;

        public SimpleActionFrame(Frame parent, List<Frame> frames, VarTable varTable, ParsedAction<?> action, QuestContext questContext) {
            super(parent, frames, varTable, questContext);
            this.action = action;
        }

        @Override
        public String name() {
            return this.action.toString();
        }

        @Override
        public Optional<ParsedAction<?>> currentAction() {
            return Optional.of(action);
        }

        @Override
        public void setNext(@NotNull ParsedAction<?> action) {
            if (this.parent != null) {
                this.parent.setNext(action);
            }
        }

        @Override
        public void setNext(@NotNull Quest.Block block) {
            if (this.parent != null) {
                this.parent.setNext(block);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> CompletableFuture<T> run() {
            // 占位 future 用于抢占运行权，随后再替换为动作真正返回的 future
            CompletableFuture<Object> placeholder = new CompletableFuture<>();
            Preconditions.checkState(this.futureRef.compareAndSet(null, placeholder), "already running");
            this.varTable.initialize(this);
            CompletableFuture<?> actionFuture;
            try {
                actionFuture = Objects.requireNonNull(this.action.process(this), "Quest action returned null future: " + action);
            } catch (Throwable ex) {
                CompletableFuture<Object> failed = new CompletableFuture<>();
                completeFailure(failed, ex);
                actionFuture = failed;
            }
            // 若在动作执行期间 frame 已被关闭（占位 future 被置空），直接取消动作，不再对外暴露
            if (!this.futureRef.compareAndSet(placeholder, actionFuture)) {
                actionFuture.cancel(false);
                return (CompletableFuture<T>) actionFuture;
            }
            return (CompletableFuture<T>) actionFuture;
        }
    }

    public static class SimpleVarTable implements VarTable {

        private final Frame parent;
        private final Map<String, Object> map;

        public SimpleVarTable(Frame parent) {
            this(parent, new HashMap<>());
        }

        public SimpleVarTable(Frame parent, Map<String, Object> map) {
            this.parent = parent;
            this.map = map;
        }

        @Override
        public VarTable parent() {
            return parent != null ? parent.variables() : null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> get(@NotNull String name) throws CompletionException {
            Object o = map.get(name);
            if (o == null && parent != null) {
                return parent.variables().get(name);
            }
            if (o instanceof QuestFuture<?>) {
                o = ((QuestFuture<?>) o).getFuture().join();
            }
            return (Optional<T>) Optional.ofNullable(o);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<QuestFuture<T>> getFuture(@NotNull String name) {
            Object o = map.get(name);
            if (o == null && parent != null) {
                return parent.variables().getFuture(name);
            }
            if (o instanceof QuestFuture) {
                return Optional.of((QuestFuture<T>) o);
            } else {
                return Optional.empty();
            }
        }

        @Override
        public void set(@NotNull String name, Object value) {
            if (name.startsWith("~") || parent() == null) {
                map.put(name, value);
            } else {
                parent().set(name, value);
            }
        }

        @Override
        public <T> void set(@NotNull String name, @NotNull ParsedAction<T> owner, @NotNull CompletableFuture<T> future) {
            this.map.put(name, new QuestFuture<>(owner, future));
        }

        @Override
        public void remove(@NotNull String name) {
            this.map.remove(name);
        }

        @Override
        public void clear() {
            this.map.clear();
        }

        @Override
        public Set<String> keys() {
            return Collections.unmodifiableSet(this.map.keySet());
        }

        @Override
        public Collection<Map.Entry<String, Object>> values() {
            return Collections.unmodifiableCollection(this.map.entrySet());
        }

        @Override
        public void initialize(@NotNull Frame frame) {
            for (Object o : this.map.values()) {
                if (o instanceof QuestFuture) {
                    ((QuestFuture<?>) o).run(frame);
                }
            }
        }

        @Override
        public void close() {
            for (Object o : this.map.values()) {
                if (o instanceof QuestFuture) {
                    ((QuestFuture<?>) o).close();
                }
            }
        }
    }
}
