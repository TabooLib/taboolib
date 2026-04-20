package taboolib.module.incision.weaver.site

import taboolib.module.incision.annotation.Trim
import taboolib.module.incision.api.Anchor
import taboolib.module.incision.api.MethodCoordinate
import taboolib.module.incision.api.Shift
import taboolib.module.incision.runtime.AdviceKind
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * 锚点描述 — 由 weaver 内部使用，对应 [taboolib.module.incision.annotation.Site] 注解。
 *
 * @property anchor 锚点类型 (INVOKE / FIELD_GET / FIELD_PUT / NEW / THROW / RETURN / HEAD / TAIL)
 * @property ownerPattern 锚点目标的 owner（internalName）；空则匹配所有
 * @property namePattern  锚点目标的 name；空则匹配所有
 * @property descPattern  锚点目标的 desc；空则匹配所有
 * @property shift   相对锚点的偏移（BEFORE / AFTER）
 * @property ordinal 第 N 次出现（0 起步），-1 表示全部
 * @property offset  沿 [shift] 方向跨越的指令条数；0 表示就在锚点处。
 * @property pattern 可选的高阶模式（来自 [SitePattern]，例如 [SitePattern.OpcodeSeq]）。
 *                   旧的三字段 ownerPattern/namePattern/descPattern 仍保留以兼容 streaming 后端。
 * @property trimKind kind=TRIM 时区分 RETURN / ARG / VAR 三种语义；其他 kind 忽略。
 * @property trimIndex ARG/VAR 模式下的实参索引或局部变量槽。
 * @property trimArgDescriptor ARG/VAR 模式下被改写的值的 JVM 描述符（用于 CHECKCAST），如 "Ljava/lang/String;"。
 * @property trimReturnDescriptor RETURN 模式下被改写的栈顶值的 JVM 描述符（INVOKE 返回类型 / FIELD_GET 字段类型 /
 *  RETURN 锚点宿主方法返回类型）。SurgeonScanner 在能从 [descPattern] / [target] 静态推断时预填；否则由
 *  [taboolib.module.incision.weaver.SiteWeaver] 在 applyPlan 阶段按 anchor 节点补齐。空串时 emitter 走兼容
 *  的"按引用 1-slot 处理"老路径。
 * @property bypassConsumedDescs BYPASS 模式下，原锚点指令在执行前消费的栈值描述符的拼接（栈底→栈顶顺序），
 *  例如 INVOKEVIRTUAL Lowner;ID 表示栈底是 receiver、然后 I、然后 D（双宽）。空串表示不消费。
 *  由 [taboolib.module.incision.weaver.SiteWeaver] 在 applyPlan 阶段按 anchor 节点填补。
 * @property bypassReturnDescriptor BYPASS 模式下，原锚点指令产生的栈顶值描述符（INVOKE 返回 / FIELD_GET 字段 /
 *  PUTFIELD/PUTSTATIC 为 "V"）。空串时 emitter 退回旧行为（按 [isVoid] 兼容路径）。
 * @property bypassTempSlot BYPASS 模式下保存 dispatch 返回 Object 的临时局部变量槽位；由 SiteWeaver 用
 *  宿主方法的 maxLocals 分配。-1 表示未分配。
 * @property adviceId 对应 [taboolib.module.incision.runtime.AdviceEntry.id]。由 SurgeonScanner 在构造 AdviceEntry
 *  时一并写入；DispatcherEmitter 把它编码进 dispatch 调用的 targetSig 后缀（`baseSig#adviceId`），
 *  [taboolib.module.incision.runtime.TheatreDispatcher.dispatch] 按此精确路由到单一 AdviceEntry。
 *  空串表示无 id（兼容早期 emission，dispatch 退化为按 phase 匹配；但多 advice 共目标会导致全员触发）。
 * @property hostMethodDescriptor 宿主方法的 JVM 描述符。site dispatch 需要它来回填原方法 args。
 * @property hostIsStatic 宿主方法是否为静态方法；为 false 时 site dispatch 会把 slot0 作为 `self` 传入。
 */
data class SiteSpec(
    val anchor: Anchor,
    val ownerPattern: String = "",
    val namePattern: String = "",
    val descPattern: String = "",
    val shift: Shift = Shift.BEFORE,
    val ordinal: Int = -1,
    val kind: AdviceKind,
    val target: MethodCoordinate,
    val offset: Int = 0,
    val pattern: SitePattern? = null,
    val trimKind: Trim.Kind? = null,
    val trimIndex: Int = 0,
    val trimArgDescriptor: String = "",
    val trimReturnDescriptor: String = "",
    val bypassConsumedDescs: String = "",
    val bypassReturnDescriptor: String = "",
    val bypassTempSlot: Int = -1,
    val adviceId: String = "",
    val hostMethodDescriptor: String = "",
    val hostIsStatic: Boolean = false,
)
