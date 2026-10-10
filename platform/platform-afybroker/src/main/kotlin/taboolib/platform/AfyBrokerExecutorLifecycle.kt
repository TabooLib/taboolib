package taboolib.platform

internal enum class AfyBrokerExecutorState {
    NEW,
    RUNNING,
    STOPPED
}

internal enum class AfyBrokerTaskRegistration {
    PENDING,
    ACTIVE,
    REJECTED
}

internal class AfyBrokerTaskRegistry<T : Any> {

    private val lock = Any()
    private val pending = LinkedHashSet<T>()
    private val active = LinkedHashSet<T>()
    private var state = AfyBrokerExecutorState.NEW

    fun register(task: T): AfyBrokerTaskRegistration {
        return synchronized(lock) {
            when (state) {
                AfyBrokerExecutorState.NEW -> {
                    pending += task
                    AfyBrokerTaskRegistration.PENDING
                }
                AfyBrokerExecutorState.RUNNING -> {
                    active += task
                    AfyBrokerTaskRegistration.ACTIVE
                }
                AfyBrokerExecutorState.STOPPED -> AfyBrokerTaskRegistration.REJECTED
            }
        }
    }

    fun start(): List<T> {
        return synchronized(lock) {
            if (state != AfyBrokerExecutorState.NEW) {
                return@synchronized emptyList()
            }
            state = AfyBrokerExecutorState.RUNNING
            val tasks = pending.toList()
            pending.clear()
            active += tasks
            tasks
        }
    }

    fun remove(task: T): Boolean {
        return synchronized(lock) {
            pending.remove(task) || active.remove(task)
        }
    }

    fun stop(): List<T> {
        return synchronized(lock) {
            if (state == AfyBrokerExecutorState.STOPPED) {
                return@synchronized emptyList()
            }
            state = AfyBrokerExecutorState.STOPPED
            val tasks = ArrayList<T>(pending.size + active.size)
            tasks += pending
            tasks += active
            pending.clear()
            active.clear()
            tasks
        }
    }

    fun state(): AfyBrokerExecutorState {
        return synchronized(lock) { state }
    }

    fun pendingCount(): Int {
        return synchronized(lock) { pending.size }
    }

    fun activeCount(): Int {
        return synchronized(lock) { active.size }
    }
}

internal class AfyBrokerTaskCancellation<T : Any>(private val cancelDelegate: (T) -> Unit) {

    private val lock = Any()

    @Volatile
    private var cancelled = false
    private var delegate: T? = null

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

    fun isCancelled(): Boolean {
        return cancelled
    }

    fun runIfActive(action: () -> Unit): Boolean {
        if (cancelled) {
            return false
        }
        action()
        return true
    }
}

internal inline fun <T> runAfyBrokerDispatch(
    reporter: (Throwable) -> Unit,
    cleanup: () -> Unit,
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

internal inline fun <T> runAfyBrokerTask(reporter: (Throwable) -> Unit, action: () -> T): T {
    try {
        return action()
    } catch (ex: Throwable) {
        try {
            reporter(ex)
        } catch (reportingFailure: Throwable) {
            ex.addSuppressed(reportingFailure)
        }
        throw ex
    }
}
