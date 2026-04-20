package taboolib.module.incision.dsl

import taboolib.module.incision.annotation.SurgeryDesk
import taboolib.module.incision.api.DescriptorCodec
import taboolib.module.incision.api.MethodCoordinate
import taboolib.module.incision.api.Theatre
import taboolib.module.incision.diagnostic.Trauma
import taboolib.module.incision.runtime.AdviceEntry
import taboolib.module.incision.runtime.AdviceKind

/**
 * 构建期 DSL — 在 `scalpel { ... }` 块内可用。
 *
 * 把用户写的 `lead("...") { ... }` 等翻译成待注册的 AdviceEntry。
 * 不直接注册，等 `provideDelegate` 拿到 id 后统一提交。
 */
class ScalpelBuilder internal constructor() {

    internal val pending = mutableListOf<PendingAdvice>()
    internal var priority: Int = 0

    fun priority(p: Int) { this.priority = p }

    fun lead(descriptor: String, handler: (Theatre) -> Unit) {
        pending += PendingAdvice(AdviceKind.LEAD, descriptor, priority, handler = { handler(it) })
    }

    fun trail(descriptor: String, handler: (Theatre) -> Unit) {
        pending += PendingAdvice(AdviceKind.TRAIL, descriptor, priority, handler = { handler(it) })
    }

    fun splice(descriptor: String, handler: (Theatre) -> Any?) {
        pending += PendingAdvice(AdviceKind.SPLICE, descriptor, priority, handler)
    }

    fun bypass(descriptor: String, handler: (Theatre) -> Any?) {
        pending += PendingAdvice(AdviceKind.BYPASS, descriptor, priority, handler)
    }

    fun excise(descriptor: String, handler: (Theatre) -> Any?) {
        pending += PendingAdvice(AdviceKind.EXCISE, descriptor, priority, handler)
    }

    /** 批量 — 在多个描述符上注册同样 advice */
    fun on(vararg descriptors: String, block: OnBlock.() -> Unit) {
        val sub = OnBlock(this, descriptors.toList())
        sub.block()
    }

    /** 条件谓词 — 附加到最后一条 advice */
    infix fun Unit.where(predicate: (Theatre) -> Boolean) {
        val last = pending.lastOrNull() ?: return
        pending[pending.lastIndex] = last.copy(predicate = predicate)
    }

    class OnBlock internal constructor(val parent: ScalpelBuilder, val descriptors: List<String>) {
        fun lead(handler: (Theatre) -> Unit) = descriptors.forEach { parent.lead(it, handler) }
        fun trail(handler: (Theatre) -> Unit) = descriptors.forEach { parent.trail(it, handler) }
        fun splice(handler: (Theatre) -> Any?) = descriptors.forEach { parent.splice(it, handler) }
        fun bypass(handler: (Theatre) -> Any?) = descriptors.forEach { parent.bypass(it, handler) }
    }

    data class PendingAdvice(
        val kind: AdviceKind,
        val rawDescriptor: String,
        val priority: Int,
        val handler: (Theatre) -> Any?,
        val predicate: ((Theatre) -> Boolean)? = null,
    )
}

/**
 * 校验 holder 必须是 `@SurgeryDesk` object。
 */
internal fun requireSurgeryDesk(holder: Any, propertyName: String): Class<*> {
    val clazz: Class<*> = holder.javaClass
    val anno = clazz.getAnnotation(SurgeryDesk::class.java)
    if (anno == null) {
        throw Trauma.Declaration.InvalidHolder(clazz.name, "class (缺少 @SurgeryDesk)")
    }
    // object 单例特征：INSTANCE 字段存在且指向 holder
    val instance = try { clazz.getDeclaredField("INSTANCE").apply { isAccessible = true }.get(null) } catch (_: Throwable) { null }
    if (instance !== holder) {
        throw Trauma.Declaration.InvalidHolder(clazz.name, "class (@SurgeryDesk 必须标注在 object 上，属性=$propertyName)")
    }
    return clazz
}

/**
 * Pending advice → AdviceEntry 列表，合成 id + 目标坐标。
 */
internal fun ScalpelBuilder.materialize(ownerId: String, holder: Any): Pair<List<MethodCoordinate>, List<AdviceEntry>> {
    val targets = mutableListOf<MethodCoordinate>()
    val entries = mutableListOf<AdviceEntry>()
    val cl = java.lang.ref.WeakReference(holder.javaClass.classLoader)
    for ((idx, p) in pending.withIndex()) {
        val parsed = DescriptorCodec.parseMethod(p.rawDescriptor)
            ?: throw Trauma.Declaration.BadDescriptor(p.rawDescriptor, "缺少 '#' 或括号")
        val coord = parsed.toCoordinate()
        targets += coord
        entries += AdviceEntry(
            id = "$ownerId@$idx",
            kind = p.kind,
            target = coord,
            priority = p.priority,
            handler = p.handler,
            predicate = p.predicate,
            classLoader = cl,
            explicitResumeRequired = false,
            sourceKind = "dsl",
        )
    }
    return targets to entries
}
