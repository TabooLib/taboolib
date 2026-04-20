package taboolib.module.incision.api

import kotlin.reflect.KClass

/**
 * 织入产物句柄 — 持久切术的运行时引用。
 *
 * 通过 [heal] 取消织入；通过 [suspend] / [resume] 临时启停而不卸下字节码。
 */
interface Suture : AutoCloseable {

    /** 唯一识别名，格式 `FQCN#propertyName` */
    val id: String

    /** 此切术覆盖的目标方法集合 */
    val targets: List<MethodCoordinate>

    /** 当前状态 */
    val state: State

    /** 声明所在的 SurgeryDesk object */
    val holder: KClass<*>

    /** 永久卸载该 advice（可能触发字节码回滚） */
    fun heal(): Boolean

    /** 临时挂起 — 字节码保留，dispatcher 跳过 handler */
    fun suspend(): Boolean

    /** 恢复挂起的切术 */
    fun resume(): Boolean

    override fun close() {
        heal()
    }

    enum class State {
        /** 已织入并启用 */
        ARMED,

        /** 已被触发过（统计用） */
        TRIGGERED,

        /** 已挂起，handler 跳过 */
        SUSPENDED,

        /** 已恢复，字节码已回滚或仅剩占位 */
        HEALED,

        /** 解析失败，运行期访问会再次抛 Trauma */
        INACTIVE_UNRESOLVED,
    }
}

/**
 * 由 `on(...)` 等批量 DSL 创建的复合句柄。
 */
interface BatchSuture : Suture {

    val children: List<Suture>

    operator fun get(childId: String): Suture?

    /** 按谓词卸载子切术，返回卸载数量 */
    fun heal(predicate: (Suture) -> Boolean): Int
}
