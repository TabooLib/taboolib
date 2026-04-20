package taboolib.module.incision.weaver.site.pattern.parser

import taboolib.module.incision.api.Anchor
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * Site target 解析策略 — 把用户写在 `@Site.target` 上的原始字符串解析为 [SitePattern]。
 *
 * 每个 [Anchor] 对应一个解析器实现；解析失败返回 `null`（调用方再决定降级或忽略）。
 * target 为空字符串视为 "不过滤"，这种情况也走对应的 parser。
 */
interface SiteTargetParser {

    /** 该 parser 适用的锚点类型（用于 [ParserRegistry] 查表）。 */
    val anchor: Anchor

    /**
     * 解析原始 target。
     * @param raw 用户写的原始字符串，可能为空。
     * @return 解析出的 [SitePattern]，失败返回 `null`。
     */
    fun parse(raw: String): SitePattern?
}
