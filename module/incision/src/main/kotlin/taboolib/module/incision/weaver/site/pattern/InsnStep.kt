package taboolib.module.incision.weaver.site.pattern

/**
 * 单条字节码指令的过滤步骤 — 模型层表示，等价于注解 [taboolib.module.incision.annotation.Step]
 * 的展平形式。
 *
 * 字段中的 `*Filter` 均为 glob 风格字符串：空串表示"不过滤"，`*` 表示通配。
 *
 * @property opcode 期望的 ASM 指令值；`-1` 表示通配。
 * @property ownerFilter 所属类（ASM internal name 或 glob）。
 * @property nameFilter 方法名 / 字段名（支持 glob）。
 * @property descFilter 方法描述符或字段类型描述符（支持 glob）。
 * @property cstFilter 常量值匹配（适用于 LDC / BIPUSH / SIPUSH 等）。
 * @property repeat 该步骤连续匹配的次数。
 */
data class InsnStep(
    val opcode: Int,
    val ownerFilter: String = "",
    val nameFilter: String = "",
    val descFilter: String = "",
    val cstFilter: String = "",
    val repeat: Int = 1,
)
