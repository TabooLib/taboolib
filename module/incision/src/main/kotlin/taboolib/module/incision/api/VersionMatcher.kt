package taboolib.module.incision.api

import taboolib.module.incision.diagnostic.Forensics

/**
 * 版本匹配器 —— 抽象"如何取当前版本"以及"如何判断版本是否落在区间内"。
 *
 * 默认实现 [MinecraftVersionMatcher] 通过反射读取 `taboolib.module.nms.MinecraftVersion.runningVersion`，
 * 当 bukkit-nms 模块不在 classpath 时回退为 [NoopVersionMatcher]（始终命中）。
 *
 * 外部插件可实现该接口，从自己的 manifest / 配置 / API 拿到版本字符串，
 * 然后在 `@Version(matcher = "com.foo.MyMatcher")` 中指定 FQCN 接入。
 */
interface VersionMatcher {

    /**
     * 取当前运行环境下的版本字符串，例如 "1.20.4"。
     * 返回 null 表示无法获取 —— 此时 [matches] 应直接返回 true（不过滤）。
     */
    fun current(): String?

    /**
     * 判断 [current] 是否落在 [start, end] 闭区间。空字符串端点视为无界。
     * 默认实现：dot-decimal 段比较；缺失段视为 0。
     */
    fun matches(start: String, end: String): Boolean {
        val cur = current() ?: return true
        if (start.isNotBlank() && compare(cur, start) < 0) return false
        if (end.isNotBlank() && compare(cur, end) > 0) return false
        return true
    }

    /** dot-decimal 段比较：1.20 < 1.20.1 < 1.21；非数字段按字典序比较。 */
    fun compare(a: String, b: String): Int {
        val sa = splitSegments(a)
        val sb = splitSegments(b)
        val n = maxOf(sa.size, sb.size)
        for (i in 0 until n) {
            val x = sa.getOrNull(i) ?: 0
            val y = sb.getOrNull(i) ?: 0
            if (x != y) return x.compareTo(y)
        }
        return 0
    }

    private fun splitSegments(s: String): List<Int> {
        // 兼容形如 "1.20.4-SNAPSHOT" 的非纯数字尾巴：忽略尾巴、按数字段比较
        return s.split('.', '-', '_', '+').mapNotNull { it.toIntOrNull() }
    }
}

/**
 * 永远命中的版本匹配器 —— 不过滤任何 advice。
 *
 * 当 [MinecraftVersionMatcher] 拿不到 NMS 类时回退到这里，保证既有测试不被破坏。
 */
object NoopVersionMatcher : VersionMatcher {
    override fun current(): String? = null
    override fun matches(start: String, end: String): Boolean = true
}

/**
 * 默认匹配器 —— 反射读取 [taboolib.module.nms.MinecraftVersion]。
 *
 * 反射失败一律退化为 [NoopVersionMatcher] 行为（current=null → matches=true）。
 */
object MinecraftVersionMatcher : VersionMatcher {

    @Volatile private var resolved: String? = null
    @Volatile private var probed = false

    override fun current(): String? {
        if (probed) return resolved
        synchronized(this) {
            if (probed) return resolved
            resolved = try {
                val cls = Class.forName("taboolib.module.nms.MinecraftVersion", false, javaClass.classLoader)
                val instance = cls.getField("INSTANCE").get(null)
                // universal craftbukkit / remap-only 环境下 minecraftVersion=UNKNOWN，
                // 此时不把 runningVersion 视作"可稳定匹配的 NMS 版本"，退回 null => matches=true。
                val mcVersion = tryRead(cls, instance, "minecraftVersion")
                if (mcVersion == null) null else tryRead(cls, instance, "runningVersion") ?: mcVersion
            } catch (_: Throwable) {
                null
            }
            probed = true
            Forensics.debug("MinecraftVersionMatcher: current=$resolved")
            return resolved
        }
    }

    private fun tryRead(cls: Class<*>, instance: Any, name: String): String? {
        for (getter in listOf(name, "get${name.replaceFirstChar { it.uppercase() }}")) {
            try {
                val v = cls.getMethod(getter).invoke(instance)?.toString()
                if (!v.isNullOrBlank() && v != "UNKNOWN") return v
            } catch (_: Throwable) {}
        }
        try {
            val v = cls.getField(name).get(instance)?.toString()
            if (!v.isNullOrBlank() && v != "UNKNOWN") return v
        } catch (_: Throwable) {}
        return null
    }
}

/**
 * 全局 matcher 仓库 —— 按 FQCN 缓存外部用户自定义匹配器实例。
 *
 * 解析顺序：
 * 1. FQCN 为空 → [MinecraftVersionMatcher]
 * 2. 找到 Kotlin object（INSTANCE 字段）→ 复用
 * 3. 否则尝试无参构造
 * 4. 全部失败 → 警告并回退 [NoopVersionMatcher]
 */
object VersionMatchers {

    private val cache = java.util.concurrent.ConcurrentHashMap<String, VersionMatcher>()

    fun resolve(fqcn: String): VersionMatcher {
        if (fqcn.isBlank()) return MinecraftVersionMatcher
        cache[fqcn]?.let { return it }
        val v = try {
            val cls = loadClass(fqcn)
            val obj = runCatching { cls.getField("INSTANCE").get(null) as? VersionMatcher }.getOrNull()
            obj ?: (cls.getDeclaredConstructor().apply { isAccessible = true }.newInstance() as VersionMatcher)
        } catch (t: Throwable) {
            Forensics.warn("VersionMatcher 解析失败: $fqcn — ${t.javaClass.name}: ${t.message}; 回退 NoopVersionMatcher")
            NoopVersionMatcher
        }
        cache[fqcn] = v
        return v
    }

    /**
     * 多 ClassLoader 探查 —— 顺序：thread context → VersionMatchers 自身 CL → system CL。
     *
     * 必要性：incision 模块由 IsolatedClassLoader 加载，看不到承载它的插件类。
     * 用户在自己插件里写 @Version(matcher = "com.foo.MyMatcher") 时，MyMatcher 在插件 CL，
     * 必须穿透 system / context CL 才能解析到。
     */
    private fun loadClass(fqcn: String): Class<*> {
        val candidates = sequenceOf(
            Thread.currentThread().contextClassLoader,
            VersionMatchers::class.java.classLoader,
            ClassLoader.getSystemClassLoader(),
        ).filterNotNull().distinct()
        var lastError: Throwable? = null
        for (cl in candidates) {
            try {
                return Class.forName(fqcn, true, cl)
            } catch (t: Throwable) {
                lastError = t
            }
        }
        throw lastError ?: ClassNotFoundException(fqcn)
    }
}
