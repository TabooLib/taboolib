package taboolib.module.incision.dsl

import taboolib.module.incision.api.Suture

/**
 * `scalpel.armOn(...)` / `scalpel.disarmOn(...)` 的返回句柄。
 *
 * 持有 builder + 目标事件类型 + 当前 arm 状态；用户在 SurgeryDesk 内接 TabooLib
 * 的 `@SubscribeEvent` 将该事件路由到 [arm] / [disarm]。
 *
 * 不将 incision 耦合到 Bukkit 事件体系，保持核心 platform 无关。
 */
class ArmTrigger internal constructor(
    val eventClass: Class<*>,
    private val block: ScalpelBuilder.() -> Unit,
    defaultArmed: Boolean,
) : AutoCloseable {

    @Volatile
    private var suture: Suture? = null

    init {
        if (defaultArmed) arm()
    }

    fun arm(): Suture {
        suture?.let { if (it.state != Suture.State.HEALED) return it }
        val s = Scalpel.transient(block)
        suture = s
        return s
    }

    fun disarm(): Boolean {
        val s = suture ?: return false
        val ok = s.heal()
        suture = null
        return ok
    }

    fun isArmed(): Boolean = suture?.state == Suture.State.ARMED

    override fun close() { disarm() }
}
