package taboolib.library.kether;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class AbstractQuestContext<T extends AbstractQuestContext<T>> implements QuestContext {

    protected final QuestService<T> service;
    protected final Frame rootFrame;
    protected final Quest quest;
    protected final QuestExecutor executor;
    protected ExitStatus exitStatus;
    protected CompletableFuture<Object> future;

    protected AbstractQuestContext(QuestService<T> service, Quest quest, String playerIdentifier) {
        this.service = service;
        this.quest = quest;
        this.rootFrame = createRootFrame();
        this.executor = new QuestExecutor(this);
    }

    protected abstract Executor createExecutor();

    protected Frame createRootFrame() {
        return new SimpleNamedFrame(null, new LinkedList<>(), new SimpleVarTable(null), QuestContext.BASE_BLOCK, this);
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
        Preconditions.checkState(future == null, "already running");
        return future = rootFrame.run().thenApply(o -> {
            if (this.exitStatus == null) {
                this.exitStatus = ExitStatus.success();
            }
            return o;
        });
    }

    @Override
    public void terminate() {
        this.rootFrame.close();
        if (future != null) {
            future.completeExceptionally(new QuestCloseException());
            future = null;
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
        protected CompletableFuture<?> future;
        protected Deque<AutoCloseable> closeables = new LinkedBlockingDeque<>();

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
            SimpleNamedFrame frame = new SimpleNamedFrame(this, new LinkedList<>(), new SimpleVarTable(this), name, context());
            this.frames.add(frame);
            return frame;
        }

        @Override
        public Frame newFrame(@NotNull ParsedAction<?> action) {
            Frame frame;
            if (action.get(ActionProperties.REQUIRE_FRAME, false)) {
                frame = new SimpleNamedFrame(this, new LinkedList<>(), new SimpleVarTable(this), "__anon__" + System.nanoTime(), context());
                frame.setNext(action);
            } else {
                frame = new SimpleActionFrame(this, new LinkedList<>(), new SimpleVarTable(this), action, context());
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

        @Override
        public void close() {
            if (this.future == null) return;
            for (Frame frame : this.frames) {
                frame.close();
            }
            this.cleanup();
            this.future = null;
        }

        @Override
        public boolean isDone() {
            return this.future == null || this.future.isDone();
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

        @Override
        @SuppressWarnings("unchecked")
        public <T> CompletableFuture<T> run() {
            Preconditions.checkState(this.future == null, "already running");
            varTable.initialize(this);
            future = new CompletableFuture<>();
            process(future);
            return (CompletableFuture<T>) future;
        }

        @SuppressWarnings("unchecked")
        private void process(CompletableFuture<?> future) {
            while (!context().getExitStatus().isPresent()) {
                this.cleanup();
                this.frames.removeIf(Frame::isDone);
                Optional<? extends ParsedAction<?>> optional = nextAction();
                if (optional.isPresent()) {
                    ParsedAction<?> action = optional.get();
                    CompletableFuture<?> newFuture = action.process(this);
                    if (!newFuture.isDone()) {
                        newFuture.thenRunAsync(() -> this.process(newFuture), context().getExecutor());
                        return;
                    } else {
                        future = newFuture;
                    }
                } else {
                    ((CompletableFuture<Object>) this.future).complete(future != null && future.isDone() ? future.join() : null);
                    return;
                }
            }
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
            Preconditions.checkState(this.future == null, "already running");
            this.varTable.initialize(this);
            return (CompletableFuture<T>) (this.future = this.action.process(this));
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
