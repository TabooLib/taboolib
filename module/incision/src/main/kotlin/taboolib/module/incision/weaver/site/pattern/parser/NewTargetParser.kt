package taboolib.module.incision.weaver.site.pattern.parser

import taboolib.module.incision.api.Anchor
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * NEW 锚点解析器 — 把 `java.util.ArrayList` 或 `java/util/ArrayList` 解析为
 * [SitePattern.TypeAlloc]。
 *
 * 兼容规则：raw 为空时 internalName 为空串，表示不过滤（匹配所有 NEW）。
 */
class NewTargetParser : SiteTargetParser {

    override val anchor: Anchor = Anchor.NEW

    override fun parse(raw: String): SitePattern {
        if (raw.isBlank()) return SitePattern.TypeAlloc("")
        return SitePattern.TypeAlloc(raw.replace('.', '/'))
    }
}
