package taboolib.module.incision.dsl

import taboolib.module.incision.annotation.SurgeryDesk
import taboolib.module.incision.api.Suture
import taboolib.module.incision.diagnostic.Forensics
import taboolib.module.incision.diagnostic.Trauma
import taboolib.module.incision.loader.Backend
import taboolib.module.incision.loader.InstrumentationBackend
import taboolib.module.incision.loader.JvmtiBackend
import taboolib.module.incision.runtime.AdviceEntry
import taboolib.module.incision.runtime.SurgeryRegistry
import taboolib.module.incision.runtime.TheatreDispatcher
import taboolib.module.incision.remap.RemapRouter
import taboolib.module.incision.weaver.Scalpel as ScalpelWeaver
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * DSL 顶层入口 —
 *
 * ```
 * @SurgeryDesk
 * object MyPatches {
 *     val tracker: Suture by scalpel {
 *         lead("org.bukkit.entity.Player#kickPlayer(*)") { theatre -> ... }
 *     }
 *
 *     fun onDemand() {
 *         scalpel.transient {
 *             splice("...") { ... }
 *         }.use { runBackup() }
 *     }
 * }
 * ```
 */
object Scalpel {

    operator fun invoke(block: ScalpelBuilder.() -> Unit): ScalpelProvider =
        ScalpelProvider(block, deferred = false, eagerArm = true)

    /** 惰性切 — 属性首次被访问或目标类加载时物理织入 */
    fun deferred(block: ScalpelBuilder.() -> Unit): ScalpelProvider =
        ScalpelProvider(block, deferred = true, eagerArm = false)

    /** 作用域切 — 在块内生效，块外自动 heal */
    fun scoped(block: ScalpelBuilder.() -> Unit): ScopedHandle {
        verifyCallerIsSurgeryDesk("scoped")
        return ScopedHandle(block)
    }

    /** 线程局部切 — 默认 disable，按线程手动 activate */
    fun threadLocal(block: ScalpelBuilder.() -> Unit): ThreadLocalSuture {
        verifyCallerIsSurgeryDesk("threadLocal")
        return ThreadLocalSuture(block)
    }

    /**
     * 事件驱动 — 监听到 [eventClass] 实例时才 arm advice。
     * 真正的事件订阅由调用方在 SurgeryDesk 内自行接 TabooLib `@SubscribeEvent`，
     * 此处仅返回一个 [ArmTrigger]，由用户主动 [ArmTrigger.arm] / [ArmTrigger.disarm]。
     *
     * 设计上：incision 不强行把 Bukkit 事件耦合进核心，避免 platform 依赖污染。
     */
    fun armOn(eventClass: Class<*>, block: ScalpelBuilder.() -> Unit): ArmTrigger {
        verifyCallerIsSurgeryDesk("armOn")
        return ArmTrigger(eventClass, block, defaultArmed = false)
    }

    fun disarmOn(eventClass: Class<*>, block: ScalpelBuilder.() -> Unit): ArmTrigger {
        verifyCallerIsSurgeryDesk("disarmOn")
        return ArmTrigger(eventClass, block, defaultArmed = true)
    }

    /**
     * 互斥块 — 块内的切术替换同一目标的其他活跃切术；块结束后恢复。
     * 实现：进入时 suspend 同 target 上其他 ARMED suture，退出时 resume。
     */
    fun <R> exclusive(block: ScalpelBuilder.() -> Unit, body: () -> R): R {
        verifyCallerIsSurgeryDesk("exclusive")
        val suture = transient(block)
        val touched = mutableListOf<taboolib.module.incision.api.Suture>()
        try {
            for (t in suture.targets) {
                for (other in SurgeryRegistry.listByTarget(t)) {
                    if (other === suture) continue
                    if (other.state == taboolib.module.incision.api.Suture.State.ARMED) {
                        if (other.suspend()) touched += other
                    }
                }
            }
            return body()
        } finally {
            for (s in touched) s.resume()
            suture.heal()
        }
    }

    /** 复制句柄结构用于 A/B — 用同一 builder 再注册一份独立的临时 suture */
    fun fork(suture: Suture, mark: String = "fork"): Suture? {
        verifyCallerIsSurgeryDesk("fork")
        // fork 仅复制声明，不触发原 suture 的 advice
        Forensics.warn("scalpel.fork(${suture.id}) — 当前实现仅复制 id 标记 '$mark'，请用 transient { } 重写副本")
        return null
    }

    /** A/B 重放 — 等价于把 suture 的 advice 复制到一个临时 suture */
    fun replay(suture: Suture): Suture? = fork(suture, "replay")

    /** 全局查询 */
    fun find(id: String): Suture? = SurgeryRegistry.find(id)

    /** 列出所有切术；支持按 holder 或 target 过滤 */
    fun list(
        holder: kotlin.reflect.KClass<*>? = null,
        target: String? = null,
    ): List<Suture> = SurgeryRegistry.list().filter { s ->
        (holder == null || s.holder == holder) &&
            (target == null || s.targets.any { it.signature.contains(target) })
    }

    /** 按 scope 子串卸载所有匹配的 suture */
    fun healAll(scope: String? = null): Int {
        val targets = SurgeryRegistry.list().filter {
            scope == null || it.id.contains(scope) || it.targets.any { t -> t.signature.contains(scope) }
        }
        var n = 0
        for (s in targets) if (s.heal()) n++
        return n
    }

    /** 压缩 — 移除所有 HEALED 状态的孤儿条目（当前 SurgeryRegistry 已自动清，留为占位） */
    fun compact(): Int {
        // SurgeryRegistry.unregister 已在 heal 时调用，无需额外 compact；预留接口
        return 0
    }

    /** 临时切 — 返回 AutoCloseable，调用点必须在 @SurgeryDesk object 内部 */
    fun transient(block: ScalpelBuilder.() -> Unit): Suture {
        val caller = verifyCallerIsSurgeryDesk("transient")
        val seq = TransientCounter.next()
        val id = "${caller.name}#anon-$seq"
        val builder = ScalpelBuilder().apply(block)
        val holder = try {
            caller.getDeclaredField("INSTANCE").apply { isAccessible = true }.get(null)
        } catch (t: Throwable) {
            throw Trauma.Declaration.InvalidHolder(caller.name, "transient 调用点必须在 object 中")
        }
        val (targets, entries) = builder.materialize(id, holder!!)
        for (e in entries) TheatreDispatcher.register(e)
        installWeaver(entries)
        val kclass = caller.kotlin
        val suture = SutureImpl(id, targets, kclass, entries)
        SurgeryRegistry.register(id, suture)
        return suture
    }

    /**
     * 安装字节码 weaver — 把目标 owner 注册到 InstrumentationBackend，
     * 触发一次 retransform，使已加载的目标类被注入 dispatcher 调用。
     *
     * 同一 owner 多次注册会累积所有 entries 的 weaver；retransform 是幂等的。
     */
    private val accumulatedTargets = java.util.concurrent.ConcurrentHashMap<String, MutableList<taboolib.module.incision.weaver.Scalpel.AdviceTargetSpec>>()
    private val activeTokens = java.util.concurrent.ConcurrentHashMap<String, Backend.BackendToken>()

    internal fun installWeaver(entries: List<AdviceEntry>) {
        if (entries.isEmpty()) return
        // Kotlin @JvmStatic 方法有两条调用路径：
        //   1. 外部类的 static bridge（Java 调用者）
        //   2. $Companion 的 instance method（Kotlin 调用者）
        // 自动扩展 companion 目标，确保两条路径都被织入
        val existingOwners = entries.map { it.target.owner }.toSet()
        val expanded = mutableListOf<AdviceEntry>()
        for (e in entries) {
            expanded += e
            if (!e.target.owner.endsWith("\$Companion")) {
                val companionOwner = e.target.owner + "\$Companion"
                // 避免与 SurgeonScanner.expandTargets 重复
                if (companionOwner !in existingOwners) {
                    val companionTarget = e.target.copy(owner = companionOwner)
                    expanded += e.copy(
                        id = e.id + "#companion",
                        target = companionTarget,
                    )
                }
            }
        }
        // 注册 companion 扩展条目到 dispatcher
        for (e in expanded) {
            if (e.id.endsWith("#companion")) {
                TheatreDispatcher.register(e)
            }
        }
        val backend = resolveBackend()
        val byOwner = expanded.groupBy { it.target.owner }
        for ((owner, group) in byOwner) {
            // NMS remap：把用户写的 net/minecraft/server/MinecraftServer
            // 转换为运行时实际类名 net/minecraft/server/v1_12_R1/MinecraftServer
            val resolvedOwner = RemapRouter.resolveOwner(owner)
            val newTargets = group.groupBy { it.target }.map { (target, targetEntries) ->
                ScalpelWeaver.AdviceTargetSpec(
                    target = target,
                    kinds = targetEntries.map { it.kind }.toSet(),
                    sites = targetEntries.mapNotNull { it.siteSpec },
                )
            }
            val allTargets = accumulatedTargets.computeIfAbsent(resolvedOwner) { mutableListOf() }
            allTargets.addAll(newTargets)
            activeTokens.remove(resolvedOwner)?.remove()
            val weaver = ScalpelWeaver(targetsByOwner = mapOf(resolvedOwner to allTargets.toList()))
            val token = backend.addTransformer(resolvedOwner) { bytes -> weaver.weave(bytes) }
            activeTokens[resolvedOwner] = token
            backend.retransform(resolvedOwner.replace('/', '.'))
            Forensics.debug("installWeaver owner=$owner resolved=$resolvedOwner advices=${group.size} total=${allTargets.size} backend=${backend.name}")
        }
    }

    private fun resolveBackend(): Backend {
        if (InstrumentationBackend.available()) return InstrumentationBackend
        if (JvmtiBackend.available()) return JvmtiBackend
        Forensics.warn("无可用的 retransform 后端，织入可能失败")
        return InstrumentationBackend
    }

    /**
     * 校验调用方是 @SurgeryDesk object。
     * 返回调用方 Class。
     */
    private fun verifyCallerIsSurgeryDesk(api: String): Class<*> {
        val stack = Thread.currentThread().stackTrace
        var firstExternal: String? = null
        for (i in 2 until stack.size) {
            val name = stack[i].className
            if (name.startsWith("taboolib.module.incision.")) continue
            // skip kotlin lambda/anonymous classes (contain $) and JDK internals
            val baseName = name.substringBefore('$')
            val cls = try {
                Class.forName(baseName, false, Thread.currentThread().contextClassLoader)
            } catch (_: Throwable) {
                try { Class.forName(baseName, false, Scalpel::class.java.classLoader) } catch (_: Throwable) { null }
            } ?: continue
            if (cls.getAnnotation(SurgeryDesk::class.java) != null) return cls
            if (firstExternal == null) firstExternal = name
        }
        throw Trauma.IllegalCallSite(
            "scalpel.$api 只能在 @SurgeryDesk object 内部调用，实际在 ${firstExternal ?: "unknown"}",
            stack.take(10).map { it.toString() }
        )
    }
}

/**
 * provideDelegate 工厂 — 属性委托创建时捕获 thisRef 与 property.name，注册到 SurgeryRegistry。
 */
class ScalpelProvider internal constructor(
    private val block: ScalpelBuilder.() -> Unit,
    private val deferred: Boolean,
    private val eagerArm: Boolean,
) {
    operator fun provideDelegate(thisRef: Any, property: KProperty<*>): ReadOnlyProperty<Any, Suture> {
        val clazz = requireSurgeryDesk(thisRef, property.name)
        val id = "${clazz.name}#${property.name}"
        val builder = ScalpelBuilder().apply(block)
        val (targets, entries) = builder.materialize(id, thisRef)
        val suture = SutureImpl(id, targets, clazz.kotlin, entries)
        SurgeryRegistry.register(id, suture)
        if (!deferred && eagerArm) {
            for (e in entries) TheatreDispatcher.register(e)
            Scalpel.installWeaver(entries)
        }
        Forensics.debug("declared id=$id deferred=$deferred targets=${targets.size}")
        return ReadOnlyProperty { _, _ -> suture }
    }
}

private object TransientCounter {
    private val counter = java.util.concurrent.atomic.AtomicLong(0)
    fun next(): Long = counter.incrementAndGet()
}
