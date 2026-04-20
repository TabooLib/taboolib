package taboolib.module.incision.annotation

/**
 * 指令模式中的单个步骤。
 *
 * 用途：
 * 描述一条字节码指令需要满足什么条件，多个步骤串联后构成 [InsnPattern]。
 *
 * 使用：
 * 最少只需要提供 [opcode]；当你要匹配调用、字段访问或常量加载时，再补充
 * [owner]、[name]、[desc]、[cst]、[repeat] 等条件。
 *
 * 效果：
 * 匹配器会按字段逐项过滤；空字符串代表“不约束”，[Op.ANY] 代表任意 opcode。
 *
 * 局限：
 * 1. `desc`、`owner` 等都基于 ASM 视角，不是 Kotlin/Java 源码写法。
 * 2. glob 适合做通配，但过度宽松时很容易把非预期指令也算进来。
 *
 * @property opcode 期望的字节码指令；[Op.ANY] 表示任意指令。
 * @property owner 调用或字段访问所属类的 ASM internal name，支持 glob。
 * @property name 方法名或字段名，支持 glob。
 * @property desc 方法描述符或字段类型描述符，支持 glob。
 * @property cst 常量值匹配，常用于 LDC、BIPUSH、SIPUSH 等指令。
 * @property repeat 连续重复命中的次数；默认为 1。
 */
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Step(
    val opcode: Op,
    val owner: String = "",
    val name: String = "",
    val desc: String = "",
    val cst: String = "",
    val repeat: Int = 1
)
