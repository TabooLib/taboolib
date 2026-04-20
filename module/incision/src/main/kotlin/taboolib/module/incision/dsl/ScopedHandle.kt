package taboolib.module.incision.dsl

import taboolib.module.incision.api.Suture
import taboolib.module.incision.api.Theatre
import taboolib.module.incision.runtime.SurgeryRegistry
import taboolib.module.incision.runtime.TheatreDispatcher

/**
 * `scalpel.scoped { }` 返回的作用域句柄。调用 [run] 进入作用域，块结束自动 heal。
 */
class ScopedHandle internal constructor(private val block: ScalpelBuilder.() -> Unit) {

    fun <R> run(body: () -> R): R {
        val suture = Scalpel.transient(block)
        return try {
            body()
        } finally {
            suture.heal()
        }
    }
}

/**
 * 线程局部切术 — 所有 advice 默认附带谓词"当前线程需已 activate"。
 */
class ThreadLocalSuture internal constructor(block: ScalpelBuilder.() -> Unit) : AutoCloseable {

    private val activeThreads: MutableSet<Long> = java.util.concurrent.ConcurrentHashMap.newKeySet()
    private val suture: Suture

    init {
        val builder = ScalpelBuilder().apply(block)
        // 给所有 pending 加上 per-thread 谓词
        val wrapped = builder.pending.map { p ->
            p.copy(predicate = { _: Theatre -> Thread.currentThread().id in activeThreads } )
        }
        builder.pending.clear()
        builder.pending.addAll(wrapped)

        val seq = System.nanoTime()
        val callerClass = Thread.currentThread().stackTrace.firstOrNull { st ->
            !st.className.startsWith("taboolib.module.incision.") &&
                !st.className.startsWith("java.") &&
                !st.className.startsWith("kotlin.")
        }?.className ?: "unknown"
        val id = "$callerClass#tls-$seq"
        val holder = try { Class.forName(callerClass).getDeclaredField("INSTANCE").apply { isAccessible = true }.get(null) } catch (_: Throwable) { null }
            ?: error("threadLocal 调用点不是 object")
        val (targets, entries) = builder.materialize(id, holder)
        for (e in entries) TheatreDispatcher.register(e)
        suture = SutureImpl(id, targets, holder::class, entries)
        SurgeryRegistry.register(id, suture)
    }

    fun activateOnCurrentThread() { activeThreads.add(Thread.currentThread().id) }
    fun deactivateOnCurrentThread() { activeThreads.remove(Thread.currentThread().id) }

    fun heal() = suture.heal()
    override fun close() { heal() }
}
