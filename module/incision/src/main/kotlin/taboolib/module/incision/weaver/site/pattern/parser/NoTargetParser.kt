package taboolib.module.incision.weaver.site.pattern.parser

import taboolib.module.incision.api.Anchor
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * 无 target 锚点解析器 — 用于 [Anchor.HEAD] / [Anchor.TAIL] / [Anchor.RETURN] / [Anchor.THROW]。
 *
 * 这些锚点不依赖具体指令内容，直接产出 [SitePattern.Anywhere]。
 * 非空 raw 视为"仅供文档说明"，不影响解析结果。
 *
 * [anchor] 由构造器传入以便 [ParserRegistry] 区分四个注册项。
 */
class NoTargetParser(override val anchor: Anchor) : SiteTargetParser {

    init {
        require(anchor == Anchor.HEAD || anchor == Anchor.TAIL || anchor == Anchor.RETURN || anchor == Anchor.THROW) {
            "NoTargetParser 只接受 HEAD / TAIL / RETURN / THROW，实际=$anchor"
        }
    }

    override fun parse(raw: String): SitePattern = SitePattern.Anywhere(anchor)
}
