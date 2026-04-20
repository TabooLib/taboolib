package taboolib.module.incision.annotation

import taboolib.module.incision.api.Anchor
import taboolib.module.incision.api.Shift

/**
 * 锚点描述。
 *
 * 用途：
 * 把“织入发生在方法里的哪里”表达成结构化参数，供 [Graft]、[Bypass]、[Trim] 等注解复用。
 *
 * 使用：
 * 通过 [anchor] 指定锚点种类，例如 `HEAD`、`INVOKE`、`FIELD_GET`；
 * 再通过 [target]、[shift]、[ordinal]、[offset] 精确限定第几个命中、前后偏移多少条指令。
 *
 * 效果：
 * 运行时会先解析出候选锚点，再结合 ordinal、offset 等条件得到最终织入位置。
 *
 * 局限：
 * 1. 锚点最终还是依赖真实字节码，源码重构可能改变命中结果。
 * 2. `ordinal = -1` 表示全部命中，若目标方法里相同位点很多，执行次数会相应增加。
 *
 * @property anchor 锚点类型，决定以哪类字节码事件作为定位基准。
 * @property target 锚点目标描述符；例如 `INVOKE` 时通常写成 `类#方法名(参数)返回`。
 * @property shift 相对锚点的方向，通常为 BEFORE 或 AFTER。
 * @property ordinal 第几个命中；`-1` 表示全部命中。
 * @property offset 沿 [shift] 方向再额外跨越的指令数；`0` 表示停在锚点本身。
 */
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Site(
    val anchor: Anchor,
    val target: String = "",
    val shift: Shift = Shift.BEFORE,
    val ordinal: Int = -1,
    val offset: Int = 0,
)
