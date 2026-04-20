package taboolib.module.incision.weaver.site.pattern

import taboolib.module.incision.api.Anchor

/**
 * Site 模式 —— 描述如何在目标方法字节码中定位匹配点的模型层 sealed class。
 *
 * 由 weaver-eng 的解析器（`weaver/site/parser/`）产出，被 weaver 消费用于真实的指令匹配。
 *
 * 各分支代表不同的匹配策略：
 *  - [MethodCall] : INVOKEVIRTUAL / INVOKESPECIAL / INVOKESTATIC / INVOKEINTERFACE 调用点
 *  - [FieldAccess]: GETFIELD / PUTFIELD / GETSTATIC / PUTSTATIC 字段访问点
 *  - [TypeAlloc] : NEW 指令（对象构造）
 *  - [Anywhere]  : 方法语义级锚点（HEAD/TAIL/RETURN/THROW），不依赖指令内容
 *  - [OpcodeSeq] : 由 [InsnStep] 组成的连续指令序列
 */
sealed class SitePattern {

    /**
     * 方法调用匹配 — 各字段支持 glob 通配，空串表示不过滤。
     */
    data class MethodCall(
        val owner: String,
        val name: String,
        val desc: String,
    ) : SitePattern()

    /**
     * 字段访问匹配 — 各字段支持 glob 通配，空串表示不过滤。
     */
    data class FieldAccess(
        val owner: String,
        val name: String,
        val desc: String,
    ) : SitePattern()

    /**
     * 对象构造匹配 — [internalName] 为被构造类的 ASM internal name（如 `java/util/ArrayList`）。
     */
    data class TypeAlloc(
        val internalName: String,
    ) : SitePattern()

    /**
     * 方法语义锚点 — 仅依赖锚点类型，不需匹配具体指令内容。
     * 仅接受 [Anchor.HEAD] / [Anchor.TAIL] / [Anchor.RETURN] / [Anchor.THROW]。
     */
    data class Anywhere(
        val anchor: Anchor,
    ) : SitePattern()

    /**
     * 指令序列匹配 — 按 [steps] 顺序匹配若干条连续字节码指令。
     */
    data class OpcodeSeq(
        val steps: List<InsnStep>,
    ) : SitePattern()
}
