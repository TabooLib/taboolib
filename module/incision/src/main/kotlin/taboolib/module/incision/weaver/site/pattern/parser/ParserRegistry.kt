package taboolib.module.incision.weaver.site.pattern.parser

import taboolib.module.incision.api.Anchor
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * Site target parser 注册中心 —— 按 [Anchor] 分派到对应 [SiteTargetParser]。
 *
 * 默认注册所有内置 parser：
 *  - INVOKE        → [InvokeTargetParser]
 *  - FIELD_GET     → [FieldTargetParser] (FIELD_GET)
 *  - FIELD_PUT     → [FieldTargetParser] (FIELD_PUT)
 *  - NEW           → [NewTargetParser]
 *  - HEAD/TAIL/RETURN/THROW → [NoTargetParser]
 *
 * 使用 `@JvmField` 的 [DEFAULT] 实例即可满足 SurgeonScanner 的调用需求；
 * 如需自定义扩展（例如新加 InsnPattern 走字面量识别的解析器），可 new 出新 Registry
 * 并用 [register] 覆盖/新增。
 */
class ParserRegistry private constructor(
    private val parsers: Map<Anchor, SiteTargetParser>,
) {

    /**
     * 根据锚点解析 raw target，产出 [SitePattern]。
     * 若对应锚点未注册 parser，返回 `null`；parser 本身解析失败也返回 `null`。
     */
    fun parse(anchor: Anchor, raw: String): SitePattern? {
        val parser = parsers[anchor] ?: return null
        return parser.parse(raw)
    }

    /** 获取指定锚点的 parser（未注册返回 null）。用于单测或高级定制。 */
    fun parserOf(anchor: Anchor): SiteTargetParser? = parsers[anchor]

    /** 以当前 registry 为基础派生一份新 registry，覆盖或新增 parser。 */
    fun register(parser: SiteTargetParser): ParserRegistry =
        ParserRegistry(parsers + (parser.anchor to parser))

    companion object {

        /** 默认注册表 —— 覆盖所有 [Anchor] 枚举值。 */
        @JvmField
        val DEFAULT: ParserRegistry = ParserRegistry(
            mapOf(
                Anchor.INVOKE to InvokeTargetParser(),
                Anchor.FIELD_GET to FieldTargetParser(Anchor.FIELD_GET),
                Anchor.FIELD_PUT to FieldTargetParser(Anchor.FIELD_PUT),
                Anchor.NEW to NewTargetParser(),
                Anchor.HEAD to NoTargetParser(Anchor.HEAD),
                Anchor.TAIL to NoTargetParser(Anchor.TAIL),
                Anchor.RETURN to NoTargetParser(Anchor.RETURN),
                Anchor.THROW to NoTargetParser(Anchor.THROW),
            )
        )
    }
}
