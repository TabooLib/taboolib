package taboolib.common.platform.service

import taboolib.common.LifeCycle
import taboolib.common.platform.function.registerLifeCycleTask
import java.io.Closeable
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 平台执行器的生命周期状态
 *
 * NEW -> RUNNING -> STOPPED，单向流转，STOPPED 为终态。
 */
enum class PlatformExecutorState {

    /** 尚未启动，提交的任务进入等待队列 */
    NEW,

    /** 已启动，提交的任务立即调度 */
    RUNNING,

    /** 已停止，拒绝一切新任务 */
    STOPPED
}

/**
 * 任务登记结果
 */
enum class PlatformTaskRegistration {

    /** 已进入等待队列，待执行器启动后统一调度 */
    PENDING,

    /** 已进入活动队列，需要立即调度 */
    ACTIVE,

    /** 执行器已停止，任务被拒绝 */
    REJECTED
}

/**
 * 命名线程工厂，用于给平台异步线程池的线程赋予可读名称
 *
 * @param namePrefix 线程名前缀，实际名称为「前缀 + 自增序号」
 */
class PlatformThreadFactory(private val namePrefix: String) : ThreadFactory {

    private val counter = AtomicInteger()

    override fun newThread(runnable: Runnable): Thread {
        return Thread(runnable, "$namePrefix${counter.incrementAndGet()}")
    }
}

/**
 * 异常聚合器
 *
 * 用于「批量操作中某一步失败仍需继续执行剩余步骤」的场景：
 * 首个异常作为主异常，后续异常通过 [Throwable.addSuppressed] 附加，最后统一抛出。
 */
class PlatformFailureCollector {

    private var failure: Throwable? = null

    /** 执行动作并捕获异常 */
    inline fun collect(action: () -> Unit) {
        try {
            action()
        } catch (ex: Throwable) {
            record(ex)
        }
    }

    /** 记录一个异常 */
    fun record(ex: Throwable) {
        val current = failure
        if (current == null) {
            failure = ex
        } else {
            current.addSuppressed(ex)
        }
    }

    /** 若存在异常则抛出 */
    fun rethrow() {
        failure?.let { throw it }
    }
}

/**
 * 调度句柄取消协调器
 *
 * 解决「任务在拿到调度句柄之前就被取消」的竞态：
 * - 先取消后绑定：绑定时立即取消句柄
 * - 先绑定后取消：取消时取消句柄
 * 无论何种顺序，[cancelDelegate] 至多执行一次。
 *
 * @param cancelDelegate 取消底层调度句柄的动作
 */
class PlatformTaskCancellation<T : Any>(private val cancelDelegate: (T) -> Unit) {

    private val lock = Any()

    @Volatile
    private var cancelled = false
    private var delegate: T? = null

    /**
     * 绑定调度句柄，允许对同一实例重复绑定
     *
     * @throws IllegalStateException 绑定了不同的句柄
     */
    fun bind(value: T) {
        val cancelNow = synchronized(lock) {
            val current = delegate
            check(current == null || current === value) { "Scheduled task is already bound" }
            if (current == null) {
                delegate = value
                cancelled
            } else {
                false
            }
        }
        if (cancelNow) {
            cancelDelegate(value)
        }
    }

    /**
     * 取消任务，幂等
     *
     * @param afterCancellation 取消后的清理动作，即便 [cancelDelegate] 抛出也会执行
     * @return 本次调用是否真正执行了取消
     */
    fun cancel(afterCancellation: () -> Unit = {}): Boolean {
        val bound = synchronized(lock) {
            if (cancelled) {
                return false
            }
            cancelled = true
            delegate
        }
        try {
            if (bound != null) {
                cancelDelegate(bound)
            }
        } finally {
            afterCancellation()
        }
        return true
    }

    /** 是否已取消 */
    fun isCancelled(): Boolean {
        return cancelled
    }

    /**
     * 仅在任务未取消时执行动作
     *
     * @return 动作是否被执行
     */
    fun runIfActive(action: () -> Unit): Boolean {
        if (cancelled) {
            return false
        }
        action()
        return true
    }
}

/**
 * 任务登记表
 *
 * 维护 [PlatformExecutorState] 状态机与「等待队列 / 活动队列」双集合，所有操作在同一把锁下完成。
 */
class PlatformTaskRegistry<T : Any> {

    private val lock = Any()
    private val pending = LinkedHashSet<T>()
    private val active = LinkedHashSet<T>()
    private var state = PlatformExecutorState.NEW

    /** 登记任务 */
    fun register(task: T): PlatformTaskRegistration {
        return synchronized(lock) {
            when (state) {
                PlatformExecutorState.NEW -> {
                    pending += task
                    PlatformTaskRegistration.PENDING
                }
                PlatformExecutorState.RUNNING -> {
                    active += task
                    PlatformTaskRegistration.ACTIVE
                }
                PlatformExecutorState.STOPPED -> PlatformTaskRegistration.REJECTED
            }
        }
    }

    /**
     * 由 NEW 转入 RUNNING，并将等待队列中的任务转入活动队列
     *
     * @param accept 过滤器，返回 false 的任务被直接丢弃（例如已取消的任务）
     * @return 需要立即调度的任务，若状态不是 NEW 则返回空列表
     */
    fun start(accept: (T) -> Boolean = { true }): List<T> {
        return synchronized(lock) {
            if (state != PlatformExecutorState.NEW) {
                return@synchronized emptyList<T>()
            }
            state = PlatformExecutorState.RUNNING
            val tasks = pending.filterTo(ArrayList(), accept)
            pending.clear()
            active += tasks
            tasks
        }
    }

    /** 从队列中移除任务 */
    fun remove(task: T): Boolean {
        return synchronized(lock) {
            pending.remove(task) || active.remove(task)
        }
    }

    /**
     * 转入 STOPPED 并清空两个队列
     *
     * @return 需要取消的任务，若此前已经停止则返回 null
     */
    fun stop(): List<T>? {
        return synchronized(lock) {
            if (state == PlatformExecutorState.STOPPED) {
                return@synchronized null
            }
            state = PlatformExecutorState.STOPPED
            val tasks = ArrayList<T>(pending.size + active.size)
            tasks += pending
            tasks += active
            pending.clear()
            active.clear()
            tasks
        }
    }

    /** 当前状态 */
    fun state(): PlatformExecutorState {
        return synchronized(lock) { state }
    }

    /** 等待队列长度 */
    fun pendingCount(): Int {
        return synchronized(lock) { pending.size }
    }

    /** 活动队列长度 */
    fun activeCount(): Int {
        return synchronized(lock) { active.size }
    }
}

/**
 * 基于 [Closeable] 的幂等平台任务句柄
 *
 * 多次调用 [cancel] 只会触发一次 [runnable]。
 */
open class CloseablePlatformTask(val runnable: Closeable) : PlatformExecutor.PlatformTask {

    private val cancelled = AtomicBoolean(false)

    override fun cancel() {
        if (cancelled.compareAndSet(false, true)) {
            runnable.close()
        }
    }
}

/**
 * 平台执行器公共基类
 *
 * 抽取 Velocity / Application / AfyBroker / Hytale 等平台执行器中完全一致的部分：
 * - NEW / RUNNING / STOPPED 状态机与等待、活动双队列
 * - DISABLE 生命周期下的停止任务注册
 * - 启动、停止过程中的异常聚合（首个异常为主，其余 addSuppressed）
 * - 停止后拒绝提交任务
 *
 * 各平台只需实现自己的调度差异（[launchTask]、[cancelTask]、[onStopped]）。
 *
 * 注意：本类不实现 [PlatformExecutor]，由各平台执行器显式声明该接口，
 * 以保证 PlatformFactory 能够通过「直接实现的接口」识别平台服务。
 *
 * @param executorName 执行器名称，用于拒绝任务时的异常信息
 */
abstract class PlatformExecutorSupport<T : Any>(private val executorName: String) {

    private val registry = PlatformTaskRegistry<T>()

    /**
     * 在 DISABLE 生命周期（优先级 2）下注册停止任务
     *
     * 必须由子类在自身 init 块的末尾调用，以确保子类字段已完成初始化。
     */
    protected fun registerStopTaskOnDisable() {
        registerLifeCycleTask(LifeCycle.DISABLE, 2) { stop() }
    }

    /**
     * 停止执行器，幂等
     */
    open fun stop() {
        stopTasks()
    }

    /**
     * 启动执行器：转入 RUNNING 并调度全部等待中的任务
     *
     * 单个任务调度失败不会中断其余任务，所有异常在最后统一抛出。
     */
    protected fun startTasks() {
        val tasks = registry.start { !isTaskCancelled(it) }
        val collector = PlatformFailureCollector()
        tasks.forEach { task -> collector.collect { launchTask(task) } }
        collector.rethrow()
    }

    /**
     * 停止执行器：转入 STOPPED，取消全部任务并释放平台资源
     *
     * 单个任务取消失败不会中断其余任务，所有异常在最后统一抛出。
     */
    protected fun stopTasks() {
        val tasks = registry.stop() ?: return
        val collector = PlatformFailureCollector()
        tasks.forEach { task -> collector.collect { cancelTask(task) } }
        collector.collect { onStopped() }
        collector.rethrow()
    }

    /** 登记任务 */
    protected fun registerTask(task: T): PlatformTaskRegistration {
        return registry.register(task)
    }

    /** 任务结束（正常完成或被取消），从队列中移除 */
    protected fun taskFinished(task: T) {
        registry.remove(task)
    }

    /** 抛出「执行器已停止」异常 */
    protected fun rejectStopped(): Nothing {
        throw RejectedExecutionException("$executorName has been stopped")
    }

    /** 若执行器已停止则拒绝 */
    protected fun rejectIfStopped() {
        if (registry.state() == PlatformExecutorState.STOPPED) {
            rejectStopped()
        }
    }

    /** 当前状态 */
    fun currentState(): PlatformExecutorState {
        return registry.state()
    }

    /** 等待中的任务数量 */
    fun pendingTaskCount(): Int {
        return registry.pendingCount()
    }

    /** 活动中的任务数量 */
    fun activeTaskCount(): Int {
        return registry.activeCount()
    }

    /** 调度任务，由各平台实现 */
    protected abstract fun launchTask(task: T)

    /** 取消任务，由各平台实现 */
    protected abstract fun cancelTask(task: T)

    /** 判断任务是否已取消，用于启动时跳过已取消的等待任务 */
    protected open fun isTaskCancelled(task: T): Boolean = false

    /** 停止后的资源释放钩子，例如关闭异步线程池 */
    protected open fun onStopped() {}
}

/**
 * 执行动作并在失败时上报异常
 *
 * 上报器与清理动作自身抛出的异常通过 [Throwable.addSuppressed] 附加到原异常上，
 * 保证原始异常不被替换。
 *
 * @param reporter 异常上报器
 * @param cleanup  失败后的清理动作
 * @param action   实际动作
 */
inline fun <T> runReportingFailure(
    reporter: (Throwable) -> Unit,
    cleanup: () -> Unit = {},
    action: () -> T,
): T {
    try {
        return action()
    } catch (ex: Throwable) {
        try {
            reporter(ex)
        } catch (reportingFailure: Throwable) {
            ex.addSuppressed(reportingFailure)
        }
        try {
            cleanup()
        } catch (cleanupFailure: Throwable) {
            ex.addSuppressed(cleanupFailure)
        }
        throw ex
    }
}
