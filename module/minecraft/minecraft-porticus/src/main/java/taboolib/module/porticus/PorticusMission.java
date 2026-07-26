package taboolib.module.porticus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * Porticus
 * taboolib.module.porticus.api.Mission
 * <p>
 * 通讯任务
 *
 * @author bkm016
 * @since 2020/10/15 9:55 下午
 */
public abstract class PorticusMission {

    protected final UUID uid;

    protected Consumer<String[]> consumer;
    protected Runnable runnable;
    protected String[] command;
    protected long timeout = TimeUnit.SECONDS.toMillis(10);
    private volatile long start;
    private volatile boolean started;
    LongSupplier timeSource = System::currentTimeMillis;

    public PorticusMission() {
        this(UUID.randomUUID());
    }

    public PorticusMission(UUID uid) {
        this.uid = uid;
    }

    /**
     * 通讯任务是否超时
     */
    public boolean isTimeout() {
        long startedAt = start;
        return started && timeSource.getAsLong() - startedAt >= timeout;
    }

    /**
     * 运行通讯任务
     *
     * @param target 发送目标，根据服务端类型传入对应玩家对象，当 API 类型为 SERVER 时传入 ProxyPlayer 类型，为 CLIENT 时则传入 Player 类型。
     */
    public synchronized void run(@NotNull Object target) {
        if (started) {
            throw new IllegalStateException("Porticus missions can only be run once");
        }
        boolean trackCompletion = consumer != null || runnable != null;
        if (trackCompletion) {
            // start 与 started 必须在入列之前赋值：
            // start 默认为 0 时 isTimeout() 恒为 true，若超时扫描线程在赋值之前取到该任务会立即误判超时。
            this.start = timeSource.getAsLong();
            this.started = true;
            // UID 唯一性由 Map 结构保证，无需线性查重
            PorticusMission existing = Porticus.INSTANCE.getMissions().putIfAbsent(uid, this);
            if (existing != null) {
                this.started = false;
                throw new IllegalStateException("A Porticus mission with the same UID is already pending");
            }
        } else {
            this.start = timeSource.getAsLong();
            this.started = true;
        }
    }

    /**
     * 该任务是否仍处于等待回执的状态。
     * <p>
     * 任务被超时扫描、回执事件或发送失败任一方裁决后即从表中移除，此后返回 false。
     */
    public boolean isPending() {
        return Porticus.INSTANCE.getMissions().get(uid) == this;
    }

    /**
     * 创建通讯任务的回执执行动作
     * 当对方通过 response() 方法返回信息时，该动作将会被执行。
     *
     * @param consumer 动作
     * @return {@link PorticusMission}
     */
    @NotNull
    public PorticusMission onResponse(@Nullable Consumer<String[]> consumer) {
        this.consumer = consumer;
        return this;
    }

    /**
     * 创建通讯任务当超时动作
     * 当对方没有给你任何回应时，该动作将会被执行。
     *
     * @param runnable 动作
     * @return {@link PorticusMission}
     */
    @NotNull
    public PorticusMission onTimeout(@Nullable Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    /**
     * 添加超时时间
     *
     * @param timeout  时间
     * @param timeUnit 时间单位
     * @return {@link PorticusMission}
     */
    @NotNull
    public PorticusMission timeout(long timeout, TimeUnit timeUnit) {
        this.timeout = timeUnit.toMillis(timeout);
        return this;
    }

    /**
     * 添加通讯命令
     *
     * @return {@link PorticusMission}
     */
    @NotNull
    public PorticusMission command(@NotNull String... args) {
        String[] command = new String[args.length + 1];
        command[0] = uid.toString();
        System.arraycopy(args, 0, command, 1, args.length);
        this.command = command;
        return this;
    }

    @Nullable
    public Consumer<String[]> getResponseConsumer() {
        return consumer;
    }

    @Nullable
    public Runnable getTimeoutRunnable() {
        return runnable;
    }

    @Nullable
    public String[] getCommand() {
        return command;
    }

    @NotNull
    public UUID getUID() {
        return uid;
    }

    public long getStart() {
        return start;
    }

    public long getTimeout() {
        return timeout;
    }
}
