package taboolib.module.incision.diagnostic

import taboolib.module.incision.api.MethodCoordinate

/**
 * Incision 错误根类 — 所有 incision 故障走此家族，便于统一 Forensics 结构化日志。
 *
 * 切术复杂度高，禁止抛 `NullPointerException` 或裸异常。每个场景都有明确子类，
 * 附带足够上下文以便"一眼看出发生了什么"。
 *
 * @property phase        阶段：DECLARATION / RESOLUTION / WEAVING / RUNTIME / ATTACH
 * @property incisionId   切术 id（若可识别，`FQCN#name`）
 * @property target       目标坐标（若已解析）
 * @property surgeonClass 声明所在类 FQCN
 * @property rawDescriptor 转译前描述符
 * @property translatedDescriptor 转译后描述符
 * @property resolverName NameResolver 类名
 * @property resolverEnv  NameResolver 环境签名
 */
sealed class Trauma(
    val phase: Phase,
    message: String,
    cause: Throwable? = null,
    val incisionId: String? = null,
    val target: MethodCoordinate? = null,
    val surgeonClass: String? = null,
    val rawDescriptor: String? = null,
    val translatedDescriptor: String? = null,
    val resolverName: String? = null,
    val resolverEnv: String? = null,
) : RuntimeException(message, cause) {

    enum class Phase { DECLARATION, RESOLUTION, WEAVING, RUNTIME, ATTACH, CONFLICT }

    // ---------------------------------------------------------------------
    // Declaration — 声明期（@Surgeon / @SurgeryDesk 扫描）
    // ---------------------------------------------------------------------
    sealed class Declaration(message: String, cause: Throwable? = null, incisionId: String? = null, surgeonClass: String? = null)
        : Trauma(Phase.DECLARATION, message, cause, incisionId, surgeonClass = surgeonClass) {

        class InvalidHolder(clazz: String, kindActual: String)
            : Declaration("@SurgeryDesk / @Surgeon 必须标注在 object 上，检测到 $kindActual '$clazz'", surgeonClass = clazz)

        class DuplicateId(id: String, existing: String)
            : Declaration("切术 ID 重复: $id (已由 $existing 占用)", incisionId = id)

        class BadDescriptor(raw: String, reason: String, cause: Throwable? = null)
            : Declaration("描述符无法解析: '$raw'，原因: $reason。期望格式: 类#方法名(参数)返回", cause)

        class BadScope(raw: String, position: Int, reason: String)
            : Declaration("Scope DSL 语法错误，位置 $position 附近: $reason。原文: '$raw'")

        class IllegalSignature(id: String, reason: String)
            : Declaration("Handler 签名非法: $reason", incisionId = id)
    }

    // ---------------------------------------------------------------------
    // Resolution — 目标解析
    // ---------------------------------------------------------------------
    sealed class Resolution(message: String, cause: Throwable? = null, incisionId: String? = null,
                            rawDescriptor: String? = null, translatedDescriptor: String? = null,
                            resolverName: String? = null, resolverEnv: String? = null)
        : Trauma(Phase.RESOLUTION, message, cause, incisionId,
                 rawDescriptor = rawDescriptor, translatedDescriptor = translatedDescriptor,
                 resolverName = resolverName, resolverEnv = resolverEnv) {

        class ClassNotFound(id: String, lastTriedName: String, resolverName: String, resolverEnv: String, cause: Throwable? = null)
            : Resolution("目标类 $lastTriedName 不存在（NameResolver=$resolverName，env=$resolverEnv）",
                         cause, id, resolverName = resolverName, resolverEnv = resolverEnv)

        class MethodNotFound(id: String, owner: String, nameAndDesc: String, candidates: List<String>)
            : Resolution("$owner 上不存在方法 $nameAndDesc；候选: ${candidates.joinToString(prefix = "[", postfix = "]")}", incisionId = id)

        class AmbiguousTarget(id: String, scope: String, matches: List<String>)
            : Resolution("scope 匹配到多个方法 (${matches.size})，需细化: '$scope' → ${matches.take(5)}", incisionId = id)

        class RemapMismatch(id: String, expected: String, actual: String, env: String)
            : Resolution("Mojang/Spigot 映射表差异 (expected=$expected, actual=$actual, env=$env)", incisionId = id)
    }

    // ---------------------------------------------------------------------
    // Weaving — 字节码织入
    // ---------------------------------------------------------------------
    sealed class Weaving(message: String, cause: Throwable? = null, incisionId: String? = null, target: MethodCoordinate? = null)
        : Trauma(Phase.WEAVING, message, cause, incisionId, target = target) {

        class FrameMismatch(id: String, target: MethodCoordinate, cause: Throwable? = null)
            : Weaving("插入 advice 后 StackMapFrame 不匹配，方法=$target", cause, id, target)

        class UnsupportedConstruct(id: String, target: MethodCoordinate, reason: String)
            : Weaving("无法对 $reason 方法织入: $target", incisionId = id, target = target)

        class AsmVerifyError(id: String, target: MethodCoordinate, verifyOutput: String, cause: Throwable? = null)
            : Weaving("ClassWriter 验证失败，target=$target\n$verifyOutput", cause, id, target)

        class RetransformRejected(id: String, target: MethodCoordinate, cause: Throwable? = null)
            : Weaving("JVM 拒绝 retransformClasses（可能改动了 schema），target=$target", cause, id, target)
    }

    // ---------------------------------------------------------------------
    // Runtime — dispatcher 调用期
    // ---------------------------------------------------------------------
    sealed class Runtime(message: String, cause: Throwable? = null, incisionId: String? = null, target: MethodCoordinate? = null)
        : Trauma(Phase.RUNTIME, message, cause, incisionId, target = target) {

        class HandlerThrew(id: String, target: MethodCoordinate, cause: Throwable)
            : Runtime("施术方法抛出异常 (advice=$id, target=$target)", cause, id, target)

        class ResumeMissing(id: String, target: MethodCoordinate)
            : Runtime("@Splice 未调用 Theatre.resume.proceed() 且无返回值，advice=$id", incisionId = id, target = target)

        class ArgCoercionFailed(id: String, expected: String, actual: String, cause: Throwable? = null)
            : Runtime("参数类型转换失败（期望 $expected，实际 $actual）", cause, id)

        class Unresolved(id: String, target: MethodCoordinate?, reason: String)
            : Runtime("运行期访问未解析的切术 $id: $reason", incisionId = id, target = target)
    }

    // ---------------------------------------------------------------------
    // Attach — self-attach
    // ---------------------------------------------------------------------
    sealed class Attach(message: String, cause: Throwable? = null)
        : Trauma(Phase.ATTACH, message, cause) {

        class NoToolsJar(javaHome: String)
            : Attach("JDK 8 attach 失败：JAVA_HOME=$javaHome 下无 tools.jar；" +
                     "请使用 JDK 而非 JRE，或引入 byte-buddy-agent 依赖")

        class AttachSelfDisabled
            : Attach("jdk.attach.allowAttachSelf=false；请通过 -Djdk.attach.allowAttachSelf=true 启用，" +
                     "或在启动参数中加入 byte-buddy-agent")

        class AgentLoadFailed(agentJar: String, cause: Throwable)
            : Attach("attach 成功但 loadAgent 失败: $agentJar", cause)

        class ReflectionBlocked(module: String, cause: Throwable)
            : Attach("JDK 17+ 模块限制，需要 --add-opens $module", cause)

        class InstrumentationUnavailable(reason: String)
            : Attach("无法获取 Instrumentation：$reason")
    }

    // ---------------------------------------------------------------------
    // Conflict — 多插件冲突
    // ---------------------------------------------------------------------
    sealed class Conflict(message: String, cause: Throwable? = null, target: MethodCoordinate? = null)
        : Trauma(Phase.CONFLICT, message, cause, target = target) {

        class MultipleExcise(target: MethodCoordinate, offenders: List<String>)
            : Conflict("同一 target 存在多个 @Excise：$target\n  ${offenders.joinToString("\n  ")}", target = target)

        class BypassOverlap(target: MethodCoordinate, offenders: List<String>)
            : Conflict("多个 @Bypass 在同一 target 上，链式顺序可能不符预期：$target\n  ${offenders.joinToString("\n  ")}", target = target)

        class GateVersionMismatch(selfApi: Int, gateApi: Int)
            : Conflict("本插件 incision api=$selfApi，在线网关 api=$gateApi；已降级到共有功能子集")

        class ClassLoaderLeaked(pluginName: String, incisionId: String)
            : Conflict("advice 的 classLoader 已回收但未 heal：plugin=$pluginName id=$incisionId")
    }

    // ---------------------------------------------------------------------
    // Predicate — 谓词 DSL（解析 / 编译 / 运行）
    // ---------------------------------------------------------------------
    sealed class Predicate(message: String, cause: Throwable? = null, incisionId: String? = null)
        : Trauma(Phase.DECLARATION, message, cause, incisionId) {

        /** 词法 / 语法错误，position 为源码字符偏移（0 起算），-1 表示未知。 */
        class SyntaxError(val source: String, val position: Int, reason: String)
            : Predicate("谓词语法错误，位置 $position 附近: $reason\n  源码: $source")

        /** 引用了未定义变量（不在 args/this/result/env/site/caller 等已知绑定中）。 */
        class UndefinedVariable(val source: String, val name: String)
            : Predicate("谓词引用了未定义变量 '$name'\n  源码: $source")

        /** 在方法调用结果上做下标访问（不允许 args.size[0] 这类形式）。 */
        class MethodIndexed(val source: String, val method: String)
            : Predicate("谓词不允许对方法调用结果再做下标访问: $method[]\n  源码: $source")

        /** 访问了未知成员（属性 / 方法），用于编译期与运行期共用。 */
        class UnknownMember(val source: String, val owner: String, val member: String)
            : Predicate("谓词访问了未知成员: $owner.$member\n  源码: $source")

        /** 类型不匹配（含 as/is/ic/ip/it 失败的强制语义、比较两侧类型不可比等）。 */
        class TypeMismatch(val source: String, reason: String)
            : Predicate("谓词类型不匹配: $reason\n  源码: $source")

        /** 运行期捕获的兜底异常，被 dispatcher 包装。 */
        class RuntimeFailure(val source: String, val adviceId: String?, cause: Throwable)
            : Predicate("谓词运行期异常 (advice=$adviceId): ${cause.message}\n  源码: $source", cause, adviceId)
    }

    // ---------------------------------------------------------------------
    // DSL 非法调用
    // ---------------------------------------------------------------------
    class IllegalCallSite(reason: String, stacktrace: List<String>)
        : Trauma(Phase.DECLARATION,
                 "scalpel.transient / DSL 必须在 @SurgeryDesk object 内部调用：$reason\n" +
                 "调用栈:\n  ${stacktrace.joinToString("\n  ")}")
}
