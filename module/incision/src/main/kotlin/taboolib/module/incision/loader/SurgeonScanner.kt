package taboolib.module.incision.loader

import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.LazyEnum
import org.tabooproject.reflex.ReflexClass
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.module.incision.annotation.Bypass
import taboolib.module.incision.annotation.Excise
import taboolib.module.incision.annotation.Graft
import taboolib.module.incision.annotation.InsnPattern
import taboolib.module.incision.annotation.KotlinTarget
import taboolib.module.incision.annotation.Lead
import taboolib.module.incision.annotation.Op
import taboolib.module.incision.annotation.Operation
import taboolib.module.incision.annotation.Site
import taboolib.module.incision.annotation.Splice
import taboolib.module.incision.annotation.Step
import taboolib.module.incision.annotation.Surgeon
import taboolib.module.incision.annotation.Trail
import taboolib.module.incision.annotation.Trim
import taboolib.module.incision.annotation.Version
import taboolib.module.incision.api.Anchor
import taboolib.module.incision.api.DescriptorCodec
import taboolib.module.incision.api.VersionMatchers

import taboolib.module.incision.api.MethodCoordinate
import taboolib.module.incision.api.Theatre
import taboolib.module.incision.diagnostic.Forensics
import taboolib.module.incision.diagnostic.Trauma
import taboolib.module.incision.dsl.SutureImpl
import taboolib.module.incision.dsl.Scalpel
import taboolib.module.incision.pred.AdviceCtx
import taboolib.module.incision.pred.PredCompiler
import taboolib.module.incision.pred.Predicate
import taboolib.module.incision.runtime.AdviceEntry
import taboolib.module.incision.runtime.AdviceKind
import taboolib.module.incision.runtime.SurgeryRegistry
import taboolib.module.incision.runtime.TheatreDispatcher
import taboolib.module.incision.weaver.site.SiteSpec
import taboolib.module.incision.weaver.site.pattern.InsnStep
import taboolib.module.incision.weaver.site.pattern.SitePattern

/**
 * @Surgeon 扫描器 —— 在 CONST 阶段扫描所有标注 @Surgeon 的 object，
 * 并且优先级早于 `ClassVisitorAwake(CONST)`，以便尽量覆盖宿主插件自己的 `@Awake(CONST)` 行为。
 * 把注解方法翻译为 advice 并注册到 TheatreDispatcher，同时触发字节码织入。
 */
@Inject
@Awake
class SurgeonScanner : ClassVisitor((-1).toByte()) {

    override fun getLifeCycle(): LifeCycle = LifeCycle.CONST

    override fun visitEnd(clazz: ReflexClass) {
        if (!clazz.hasAnnotation(Surgeon::class.java)) return
        val instance = findInstance(clazz) ?: run {
            Forensics.warn("@Surgeon ${clazz.name} 无法获取实例 (要求标注在 object 上)")
            return
        }
        val surgeon = clazz.getAnnotationIfPresent(Surgeon::class.java)!!
        val defaultPriority = surgeon.property("priority", 0)

        // 每个方法单独组一个 Suture —— id = clazz.name#operationIdSuffix@i
        // aggregate entries for weaver install + 聚合调试
        val aggregateEntries = mutableListOf<AdviceEntry>()
        val holderKClass = Class.forName(clazz.name).kotlin
        val seenSutureIds = HashSet<String>()

        for ((i, m) in clazz.structure.methods.withIndex()) {
            // 版本范围过滤 —— 标 @Version 且当前版本不在区间内的 advice 直接跳过，
            // 既不进 dispatcher 也不安装 weaver。
            if (m.isAnnotationPresent(Version::class.java)) {
                val ver = m.getAnnotation(Version::class.java)
                val start = ver.property("start", "")
                val end = ver.property("end", "")
                val matcherFqcn = ver.property("matcher", "")
                val matcher = VersionMatchers.resolve(matcherFqcn)
                if (!matcher.matches(start, end)) {
                    Forensics.debug("@Surgeon ${clazz.name}#${m.name} 因 @Version([$start, $end]) 跳过 (current=${matcher.current()})")
                    continue
                }
            }
            val operation = if (m.isAnnotationPresent(Operation::class.java)) m.getAnnotation(Operation::class.java) else null
            val methodPriority = operation?.property("priority", defaultPriority) ?: defaultPriority
            val methodEnabled = operation?.property("enabled", true) ?: true
            val methodIdSuffix = operation?.property("id", "")?.takeIf { it.isNotBlank() } ?: m.name
            // Suture id 形如 `owner#suffix@idx`：保留 @idx 确保同类内多方法共用同名 id 时互不覆盖
            val sutureId = "${clazz.name}#$methodIdSuffix@$i"
            val built = buildEntries(sutureId, methodPriority, methodEnabled, clazz, instance, m)
            if (built.isEmpty()) continue

            for (e in built) TheatreDispatcher.register(e)
            aggregateEntries += built
            val suture = SutureImpl(sutureId, built.map { it.target }.distinct(), holderKClass, built)
            try {
                SurgeryRegistry.register(sutureId, suture)
                seenSutureIds += sutureId
            } catch (t: Trauma.Declaration.DuplicateId) {
                Forensics.warn("@Surgeon 重复注册被跳过: $sutureId")
            }
        }
        if (aggregateEntries.isEmpty()) return
        // weaver 按类粒度一次性安装即可（内部按 owner 分组织入）
        Scalpel.installWeaver(aggregateEntries)
        Forensics.debug("Surgeon 扫描: ${clazz.name} → ${seenSutureIds.size} suture / ${aggregateEntries.size} advice")
    }

    private fun buildEntries(
        id: String,
        priority: Int,
        enabled: Boolean,
        owner: ReflexClass,
        instance: Any,
        m: ClassMethod,
    ): List<AdviceEntry> {
        val declaration = parseAdviceDeclaration(m) ?: return emptyList()
        val parsed = DescriptorCodec.parseMethod(declaration.rawDescriptor) ?: run {
            Forensics.warn("@Surgeon 描述符无法解析: ${declaration.rawDescriptor} (来源=${owner.name}#${m.name})")
            return emptyList()
        }
        val aliases = expandTargets(parsed.toCoordinate(), m)
        val handler: (Theatre) -> Any? = buildHandler(owner, instance, m)
        val classLoader = Class.forName(owner.name).classLoader
        val cl = java.lang.ref.WeakReference(classLoader)
        // 若 InsnPattern steps 非空，把 OpcodeSeq 注入 siteSpec.pattern（recording 路径消费）。
        val seqPattern = if (declaration.patternDeclared) SitePattern.OpcodeSeq(declaration.insnSteps) else null
        // 对于 scope-based advice（Lead/Trail/Splice/Excise），原本没有 siteSpec；
        // 带 @InsnPattern 时补一个 Anchor.HEAD + pattern 的占位 siteSpec 让 SiteWeaver 走 recording 路径。
        val siteSpecWithPattern = when {
            declaration.siteSpec != null && seqPattern != null -> declaration.siteSpec.copy(pattern = seqPattern)
            declaration.siteSpec != null -> declaration.siteSpec
            seqPattern != null -> SiteSpec(
                anchor = Anchor.HEAD,
                kind = declaration.kind,
                target = MethodCoordinate("", "", ""),
                pattern = seqPattern,
            )
            else -> null
        }
        // 编译 `where` 字符串谓词（空串或编译失败都视为无）。
        val compiledPred: Predicate? = declaration.where.takeIf { it.isNotBlank() }?.let { src ->
            try {
                PredCompiler.compile(src, AdviceCtx(adviceId = id, classLoader = classLoader))
            } catch (t: Throwable) {
                Forensics.warn("@Surgeon where 谓词编译失败: $src (${owner.name}#${m.name}) - ${t.message}")
                null
            }
        }
        return aliases.mapIndexed { index, target ->
            val entryId = if (aliases.size == 1) id else "$id#$index"
            AdviceEntry(
                id = entryId,
                kind = declaration.kind,
                target = target,
                priority = priority,
                handler = handler,
                classLoader = cl,
                explicitResumeRequired = declaration.explicitResumeRequired,
                sourceKind = "annotation",
                aliasGroup = id,
                siteSpec = siteSpecWithPattern?.copy(target = target, adviceId = entryId),
                compiledPredicate = compiledPred,
                predicateSource = declaration.where.ifBlank { null },
                enabled = enabled,
                onThrow = declaration.onThrow,
            )
        }
    }

    private fun parseAdviceDeclaration(m: ClassMethod): AdviceDeclaration? {
        if (m.isAnnotationPresent(Lead::class.java)) {
            val ann = m.getAnnotation(Lead::class.java)
            val scope = ann.property("scope", "")
            if (scope.isBlank()) return null
            return AdviceDeclaration(
                kind = AdviceKind.LEAD,
                rawDescriptor = extractMethodDescriptor(scope),
                insnSteps = readInsnSteps(ann.properties()["pattern"]),
                patternDeclared = hasInsnPattern(ann.properties()["pattern"]),
                where = ann.property("where", ""),
            )
        }
        if (m.isAnnotationPresent(Trail::class.java)) {
            val ann = m.getAnnotation(Trail::class.java)
            val scope = ann.property("scope", "")
            if (scope.isBlank()) return null
            return AdviceDeclaration(
                kind = AdviceKind.TRAIL,
                rawDescriptor = extractMethodDescriptor(scope),
                insnSteps = readInsnSteps(ann.properties()["pattern"]),
                patternDeclared = hasInsnPattern(ann.properties()["pattern"]),
                where = ann.property("where", ""),
                onThrow = ann.property("onThrow", true),
            )
        }
        if (m.isAnnotationPresent(Splice::class.java)) {
            val ann = m.getAnnotation(Splice::class.java)
            val scope = ann.property("scope", "")
            if (scope.isBlank()) return null
            return AdviceDeclaration(
                kind = AdviceKind.SPLICE,
                rawDescriptor = extractMethodDescriptor(scope),
                explicitResumeRequired = true,
                insnSteps = readInsnSteps(ann.properties()["pattern"]),
                patternDeclared = hasInsnPattern(ann.properties()["pattern"]),
                where = ann.property("where", ""),
            )
        }
        if (m.isAnnotationPresent(Bypass::class.java)) {
            val ann = m.getAnnotation(Bypass::class.java)
            val method = ann.property("method", "")
            if (method.isBlank()) return null
            val site = toSiteAnnotation(ann.properties()["site"], Site(Anchor.HEAD))
            return AdviceDeclaration(
                kind = AdviceKind.BYPASS,
                rawDescriptor = method,
                siteSpec = if (isWholeMethodBypass(site, ann.properties()["pattern"])) null else toSiteSpec(site, AdviceKind.BYPASS),
                insnSteps = readInsnSteps(ann.properties()["pattern"]),
                patternDeclared = hasInsnPattern(ann.properties()["pattern"]),
                where = ann.property("where", ""),
            )
        }
        if (m.isAnnotationPresent(Excise::class.java)) {
            val ann = m.getAnnotation(Excise::class.java)
            val scope = ann.property("scope", "")
            if (scope.isBlank()) return null
            return AdviceDeclaration(
                kind = AdviceKind.EXCISE,
                rawDescriptor = extractMethodDescriptor(scope),
                insnSteps = readInsnSteps(ann.properties()["pattern"]),
                patternDeclared = hasInsnPattern(ann.properties()["pattern"]),
                where = ann.property("where", ""),
            )
        }
        if (m.isAnnotationPresent(Graft::class.java)) {
            val ann = m.getAnnotation(Graft::class.java)
            val method = ann.property("method", "")
            if (method.isBlank()) return null
            val site = toSiteAnnotation(ann.properties()["site"], Site(Anchor.HEAD))
            return AdviceDeclaration(
                kind = AdviceKind.GRAFT,
                rawDescriptor = method,
                siteSpec = toSiteSpec(site, AdviceKind.GRAFT),
                insnSteps = readInsnSteps(ann.properties()["pattern"]),
                patternDeclared = hasInsnPattern(ann.properties()["pattern"]),
                where = ann.property("where", ""),
            )
        }
        if (m.isAnnotationPresent(Trim::class.java)) {
            val ann = m.getAnnotation(Trim::class.java)
            val method = ann.property("method", "")
            if (method.isBlank()) return null
            val trimKind = runCatching { Trim.Kind.valueOf(ann.enumName("kind", Trim.Kind.RETURN.name)) }
                .getOrDefault(Trim.Kind.RETURN)
            // TRIM 默认锚点按 kind 决定：RETURN → Anchor.RETURN（每个 IRETURN/.../ARETURN 之前栈顶就是返回值）；
            // ARG / VAR → Anchor.HEAD（从方法头部开始读 LV slot 替换）。否则 HEAD 上 TRIM RETURN 会在空栈
            // 上 DUP，立刻 "Cannot pop operand off an empty stack"。
            val defaultAnchor = if (trimKind == Trim.Kind.RETURN) Anchor.RETURN else Anchor.HEAD
            val site = toSiteAnnotation(ann.properties()["site"], Site(defaultAnchor))
            val trimIndex = ann.property("index", 0)
            val argDesc = if (trimKind == Trim.Kind.ARG) extractArgDescriptor(method, trimIndex) else ""
            val baseSpec = toSiteSpec(site, AdviceKind.TRIM)
            val retDesc = if (trimKind == Trim.Kind.RETURN) deriveTrimReturnDesc(baseSpec) else ""
            return AdviceDeclaration(
                kind = AdviceKind.TRIM,
                rawDescriptor = method,
                siteSpec = baseSpec.copy(
                    trimKind = trimKind,
                    trimIndex = trimIndex,
                    trimArgDescriptor = argDesc,
                    trimReturnDescriptor = retDesc,
                ),
                insnSteps = readInsnSteps(ann.properties()["pattern"]),
                patternDeclared = hasInsnPattern(ann.properties()["pattern"]),
                where = ann.property("where", ""),
            )
        }
        return null
    }

    /**
     * 静态推断 TRIM RETURN 模式下栈顶值的 JVM 描述符：
     *  - INVOKE 锚点：取 [SiteSpec.descPattern] 返回段（"...)X" 中的 X）
     *  - FIELD_GET 锚点：[SiteSpec.descPattern] 即字段类型，直接用
     *  - 其它（含 RETURN / HEAD / TAIL）：返回空，由 SiteWeaver.applyPlan 用宿主方法返回类型补齐
     */
    private fun deriveTrimReturnDesc(spec: SiteSpec): String {
        return when (spec.anchor) {
            Anchor.INVOKE -> {
                val desc = spec.descPattern
                val close = desc.indexOf(')')
                if (close in 0 until desc.length - 1) desc.substring(close + 1) else ""
            }
            Anchor.FIELD_GET -> spec.descPattern
            else -> ""
        }
    }

    /**
     * 从 `owner#name(argTypes)retType` 中提取第 [index] 个参数的 JVM 描述符。
     * 解析失败时返回空串（emitter 会保守跳过）。
     */
    private fun extractArgDescriptor(rawMethod: String, index: Int): String {
        val parsed = DescriptorCodec.parseMethod(rawMethod) ?: return ""
        val desc = parsed.descriptor
        val open = desc.indexOf('(')
        val close = desc.indexOf(')')
        if (open < 0 || close < 0 || close <= open) return ""
        val argsDesc = desc.substring(open + 1, close)
        val args = splitJvmArgDescriptors(argsDesc)
        return args.getOrNull(index) ?: ""
    }

    /** 把 `Ljava/lang/String;ILjava/util/List;[I` 拆成 [`Ljava/lang/String;`, `I`, `Ljava/util/List;`, `[I`]。 */
    private fun splitJvmArgDescriptors(args: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        while (i < args.length) {
            val start = i
            while (i < args.length && args[i] == '[') i++
            when (val c = args.getOrNull(i)) {
                'L' -> {
                    val semi = args.indexOf(';', i)
                    if (semi < 0) return out
                    out += args.substring(start, semi + 1)
                    i = semi + 1
                }
                'V', 'Z', 'B', 'S', 'I', 'J', 'F', 'D', 'C' -> {
                    out += args.substring(start, i + 1)
                    i++
                }
                null -> return out
                else -> {
                    out += c.toString()
                    i++
                }
            }
        }
        return out
    }

    /**
     * 把 @InsnPattern（注解或运行时 Map 形式）读成 [InsnStep] 列表。
     * 兼容两种 Reflex 返回形态：
     *  - 原生注解 [InsnPattern]（kotlin-reflect 直接取到）
     *  - `Map<String, Any>` 包含 `steps` 键（Reflex 展开形态）
     */
    private fun readInsnSteps(raw: Any?): List<InsnStep> {
        val stepsArr: Any? = when (raw) {
            null -> return emptyList()
            is InsnPattern -> raw.steps
            is Map<*, *> -> raw["steps"]
            else -> return emptyList()
        } ?: return emptyList()
        val list = mutableListOf<InsnStep>()
        when (stepsArr) {
            is Array<*> -> stepsArr.forEach { s -> readOneStep(s)?.let(list::add) }
            is Iterable<*> -> stepsArr.forEach { s -> readOneStep(s)?.let(list::add) }
            else -> Unit
        }
        return list
    }

    private fun hasInsnPattern(raw: Any?): Boolean = when (raw) {
        null -> false
        is InsnPattern -> true
        is Map<*, *> -> raw.containsKey("steps")
        else -> false
    }

    private fun readOneStep(raw: Any?): InsnStep? {
        return when (raw) {
            null -> null
            is Step -> InsnStep(
                opcode = raw.opcode.opcode,
                ownerFilter = raw.owner,
                nameFilter = raw.name,
                descFilter = raw.desc,
                cstFilter = raw.cst,
                repeat = raw.repeat,
            )
            is Map<*, *> -> {
                val opName = enumNameOf(raw["opcode"], Op.ANY.name)
                val op = runCatching { Op.valueOf(opName) }.getOrDefault(Op.ANY)
                InsnStep(
                    opcode = op.opcode,
                    ownerFilter = raw["owner"]?.toString() ?: "",
                    nameFilter = raw["name"]?.toString() ?: "",
                    descFilter = raw["desc"]?.toString() ?: "",
                    cstFilter = raw["cst"]?.toString() ?: "",
                    repeat = (raw["repeat"] as? Number)?.toInt() ?: 1,
                )
            }
            else -> null
        }
    }

    private fun toSiteSpec(site: Site, kind: AdviceKind): SiteSpec {
        val rawTarget = site.target.takeIf { it.isNotBlank() } ?: ""
        // 先走 SitePattern 统一解析；再降级转 SiteSpec 三字段以兼容旧 SiteWeaver
        val pattern = taboolib.module.incision.weaver.site.pattern.parser.ParserRegistry.DEFAULT
            .parse(site.anchor, rawTarget)
        var ownerPattern = ""
        var namePattern = ""
        var descPattern = ""
        when (pattern) {
            is taboolib.module.incision.weaver.site.pattern.SitePattern.MethodCall -> {
                ownerPattern = pattern.owner
                namePattern = pattern.name
                descPattern = pattern.desc
            }
            is taboolib.module.incision.weaver.site.pattern.SitePattern.FieldAccess -> {
                ownerPattern = pattern.owner
                namePattern = pattern.name
                descPattern = pattern.desc
            }
            is taboolib.module.incision.weaver.site.pattern.SitePattern.TypeAlloc -> {
                ownerPattern = pattern.internalName
            }
            is taboolib.module.incision.weaver.site.pattern.SitePattern.Anywhere -> {
                if (rawTarget.isNotEmpty()) {
                    Forensics.debug("@Site ${site.anchor} 忽略 target=$rawTarget")
                }
            }
            is taboolib.module.incision.weaver.site.pattern.SitePattern.OpcodeSeq -> {
                // OpcodeSeq 由方法级 @InsnPattern 驱动，不从 target 产生，这里不会命中
            }
            null -> {
                Forensics.warn("@Site ${site.anchor} target 解析失败: $rawTarget")
            }
        }
        return SiteSpec(
            anchor = site.anchor,
            ownerPattern = ownerPattern,
            namePattern = namePattern,
            descPattern = descPattern,
            shift = site.shift,
            ordinal = site.ordinal,
            kind = kind,
            target = MethodCoordinate("", "", ""),
            offset = site.offset,
        )
    }

    private fun toSiteAnnotation(raw: Any?, fallback: Site): Site {
        return when (raw) {
            null -> fallback
            is Site -> raw
            is Map<*, *> -> {
                val anchorName = enumNameOf(raw["anchor"], fallback.anchor.name)
                val shiftName = enumNameOf(raw["shift"], fallback.shift.name)
                val ordinal = (raw["ordinal"] as? Number)?.toInt() ?: fallback.ordinal
                val offset = (raw["offset"] as? Number)?.toInt() ?: fallback.offset
                Site(
                    anchor = runCatching { Anchor.valueOf(anchorName) }.getOrDefault(fallback.anchor),
                    target = raw["target"]?.toString() ?: fallback.target,
                    shift = runCatching { taboolib.module.incision.api.Shift.valueOf(shiftName) }.getOrDefault(fallback.shift),
                    ordinal = ordinal,
                    offset = offset,
                )
            }
            else -> fallback
        }
    }

    /** Reflex 的 enum 注解值是 [LazyEnum]（toString 不返回 enum name）。统一抽 name；非 enum 类型走 toString。 */
    private fun enumNameOf(raw: Any?, default: String): String = when (raw) {
        null -> default
        is LazyEnum -> raw.name
        else -> raw.toString()
    }

    private fun expandTargets(primary: MethodCoordinate, m: ClassMethod): List<MethodCoordinate> {
        val out = linkedSetOf(primary)
        if (!m.isAnnotationPresent(KotlinTarget::class.java)) return out.toList()
        val kt = m.getAnnotation(KotlinTarget::class.java)
        val companionInstance = kt.property("companionInstance", false)
        val jvmStaticBridge = kt.property("jvmStaticBridge", false)
        if (companionInstance && !primary.owner.endsWith("$" + "Companion")) {
            out += MethodCoordinate(primary.owner + "$" + "Companion", primary.name, primary.descriptor)
        }
        if (jvmStaticBridge) {
            // Kotlin 编译器对 @JvmStatic 方法有两条调用路径：
            // 1. 外部类的 static bridge（Java 调用者使用）
            // 2. $Companion 的 instance method（Kotlin 调用者使用）
            // 必须同时织入两者
            if (primary.owner.endsWith("$" + "Companion")) {
                out += MethodCoordinate(primary.owner.removeSuffix("$" + "Companion"), primary.name, primary.descriptor)
            } else {
                out += MethodCoordinate(primary.owner + "$" + "Companion", primary.name, primary.descriptor)
            }
        }
        return out.toList()
    }

    /**
     * 构建 handler —— 要求用户方法签名为 `fun(theatre: Theatre): R`。
     */
    private fun buildHandler(owner: ReflexClass, instance: Any, m: ClassMethod): (Theatre) -> Any? {
        val cls = Class.forName(owner.name)
        val jmethod = cls.declaredMethods.firstOrNull { it.name == m.name && it.parameterCount == 1 } ?: run {
            Forensics.warn("@Surgeon 无法定位方法 ${owner.name}#${m.name}，期望签名 fun(Theatre)")
            return { _ -> null }
        }
        jmethod.isAccessible = true
        return { theatre ->
            try {
                jmethod.invoke(instance, theatre)
            } catch (e: java.lang.reflect.InvocationTargetException) {
                throw e.cause ?: e
            }
        }
    }

    private fun extractMethodDescriptor(scope: String): String {
        val trimmed = scope.trim()
        if ('&' !in trimmed && '|' !in trimmed && !trimmed.startsWith("!") && !trimmed.startsWith("(")) {
            return trimmed.removePrefix("method:").removePrefix("class:").trim()
        }
        val idx = trimmed.indexOf("method:")
        if (idx < 0) return trimmed
        var i = idx + "method:".length
        var depth = 0
        var stop = false
        while (i < trimmed.length) {
            when (trimmed[i]) {
                '(' -> depth++
                ')' -> if (depth > 0) depth--
                '&', '|' -> if (depth == 0) stop = true
                else -> Unit
            }
            if (stop) break
            i++
        }
        return trimmed.substring(idx + "method:".length, i).trim()
    }

    private fun isWholeMethodBypass(site: Site, rawPattern: Any?): Boolean {
        return site.anchor == Anchor.HEAD &&
            site.target.isBlank() &&
            site.shift == taboolib.module.incision.api.Shift.BEFORE &&
            site.ordinal == -1 &&
            site.offset == 0 &&
            readInsnSteps(rawPattern).isEmpty()
    }

    private data class AdviceDeclaration(
        val kind: AdviceKind,
        val rawDescriptor: String,
        val explicitResumeRequired: Boolean = false,
        val siteSpec: SiteSpec? = null,
        val insnSteps: List<InsnStep> = emptyList(),
        val patternDeclared: Boolean = false,
        val where: String = "",
        val onThrow: Boolean = true,
    )
}
