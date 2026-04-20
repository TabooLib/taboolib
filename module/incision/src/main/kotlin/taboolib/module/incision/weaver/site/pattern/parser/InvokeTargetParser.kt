package taboolib.module.incision.weaver.site.pattern.parser

import taboolib.module.incision.api.Anchor
import taboolib.module.incision.api.DescriptorCodec
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * INVOKE 锚点解析器 — 把 `owner#name(args)ret` 解析为 [SitePattern.MethodCall]。
 *
 * 兼容规则（与既有 SurgeonScanner.toSiteSpec 一致，便于平滑迁移）：
 *  - raw 为空：owner/name/desc 全空，表示不过滤（匹配所有 INVOKE）。
 *  - raw 不含 `(`：只写方法名 `owner#name`，`()V` 的默认描述符按"不过滤"处理（desc 留空）。
 *  - [DescriptorCodec] 解析失败返回 null。
 */
class InvokeTargetParser : SiteTargetParser {

    override val anchor: Anchor = Anchor.INVOKE

    override fun parse(raw: String): SitePattern? {
        if (raw.isBlank()) return SitePattern.MethodCall("", "", "")
        val parsed = DescriptorCodec.parseMethod(raw) ?: return null
        val desc = if (parsed.descriptor == "()V" && '(' !in raw) "" else parsed.descriptor
        return SitePattern.MethodCall(parsed.owner, parsed.name, desc)
    }
}
