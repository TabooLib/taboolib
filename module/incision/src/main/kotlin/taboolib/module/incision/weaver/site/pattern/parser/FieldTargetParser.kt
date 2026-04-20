package taboolib.module.incision.weaver.site.pattern.parser

import taboolib.module.incision.api.Anchor
import taboolib.module.incision.api.DescriptorCodec
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * FIELD_GET / FIELD_PUT 锚点解析器 — 把 `owner#name:type` 解析为 [SitePattern.FieldAccess]。
 *
 * 兼容规则：
 *  - raw 为空：owner/name/desc 全空。
 *  - 带冒号：完整 `owner#name:Type` —— 走 [DescriptorCodec.parseField]。
 *  - 不带冒号：降级只按 `owner#name` 匹配，desc 留空。
 *  - 连 `#` 都没有：降级为只过滤 name。
 *
 * [anchor] 允许外部传入以区分 GET/PUT，默认 FIELD_GET（两者的解析逻辑完全相同，
 * 仅用于 [ParserRegistry] 的键区分）。
 */
class FieldTargetParser(override val anchor: Anchor = Anchor.FIELD_GET) : SiteTargetParser {

    init {
        require(anchor == Anchor.FIELD_GET || anchor == Anchor.FIELD_PUT) {
            "FieldTargetParser 只接受 FIELD_GET / FIELD_PUT，实际=$anchor"
        }
    }

    override fun parse(raw: String): SitePattern? {
        if (raw.isBlank()) return SitePattern.FieldAccess("", "", "")
        val parsed = DescriptorCodec.parseField(raw)
        if (parsed != null) {
            return SitePattern.FieldAccess(parsed.owner, parsed.name, parsed.descriptor)
        }
        val hash = raw.indexOf('#')
        return if (hash >= 0) {
            SitePattern.FieldAccess(
                owner = raw.substring(0, hash).replace('.', '/'),
                name = raw.substring(hash + 1),
                desc = "",
            )
        } else {
            SitePattern.FieldAccess(owner = "", name = raw, desc = "")
        }
    }
}
