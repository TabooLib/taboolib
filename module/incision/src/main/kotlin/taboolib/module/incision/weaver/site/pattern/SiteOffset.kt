package taboolib.module.incision.weaver.site.pattern

import taboolib.module.incision.api.Shift

/**
 * 锚点偏移 — 沿 [shift] 方向跨越 [units] 条字节码指令。
 *
 * `units = 0` 表示就在锚点处，不发生位移。
 *
 * @property shift 偏移方向（[Shift.BEFORE] / [Shift.AFTER]）。
 * @property units 跨越的指令条数。
 */
data class SiteOffset(
    val shift: Shift,
    val units: Int,
) {

    companion object {

        /** 锚点原位（不偏移）。 */
        @JvmField
        val NONE: SiteOffset = SiteOffset(Shift.BEFORE, 0)
    }
}
