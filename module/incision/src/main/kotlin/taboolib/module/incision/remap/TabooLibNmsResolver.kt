package taboolib.module.incision.remap

import taboolib.module.incision.diagnostic.Forensics

/**
 * TabooLib NMS 绑定 — 通过反射 `taboolib.module.nms.MinecraftVersion` 与 `RemapTranslation`
 * 实现名称转译。若任一反射失败，整个 resolver 回退为不可用。
 *
 * 设计目的：使 incision 对 `:module:bukkit-nms` 保持"可选依赖"关系。
 */
object TabooLibNmsResolver : NameResolver {

    private val minecraftVersionCls: Class<*>? = tryClass("taboolib.module.nms.MinecraftVersion")
    private val remapTranslationCls: Class<*>? = tryClass("taboolib.module.nms.remap.RemapTranslation")

    @Volatile
    private var delegate: Any? = null

    @Volatile
    private var env: String = "unavailable"

    private fun tryClass(name: String): Class<*>? = try {
        Class.forName(name, false, TabooLibNmsResolver::class.java.classLoader)
    } catch (_: Throwable) { null }

    fun installIfAvailable(): Boolean {
        val mvc = minecraftVersionCls ?: return false
        val rtc = remapTranslationCls ?: return false
        return try {
            val instance = mvc.getField("INSTANCE").get(null)
            val isUnobf = readBoolProp(mvc, instance, "isUnobfuscated") ?: false
            val isUniCB = readBoolProp(mvc, instance, "isUniversalCraftBukkit") ?: false
            val mcVer = readProp(mvc, instance, "minecraftVersion")?.toString() ?: "?"
            val isMojang = readBoolProp(mvc, instance, "isMojangMapping") ?: false

            val implClassName = when {
                isUnobf -> "taboolib.module.nms.remap.RemapTranslationUnobfsucated"
                isUniCB -> "taboolib.module.nms.remap.RemapTranslationTabooLib"
                else -> "taboolib.module.nms.remap.RemapTranslationLegacy"
            }
            val implCls = try { Class.forName(implClassName) } catch (_: Throwable) { rtc }
            delegate = implCls.getDeclaredConstructor().newInstance()
            env = "nms=$mcVer,unobf=$isUnobf,uniCB=$isUniCB,mojang=$isMojang,impl=${implCls.simpleName}"
            RemapRouter.nms = this
            Forensics.info("TabooLibNmsResolver 已启用: $env")
            true
        } catch (t: Throwable) {
            Forensics.warn("TabooLibNmsResolver 初始化失败: ${t.message}")
            false
        }
    }

    private fun readProp(cls: Class<*>, instance: Any, name: String): Any? {
        // Kotlin object 属性：先尝试 getter 方法（isXxx / getXxx），再回退到字段
        for (getter in listOf(name, "get${name.replaceFirstChar { it.uppercase() }}", "is${name.replaceFirstChar { it.uppercase() }}")) {
            try { return cls.getMethod(getter).invoke(instance) } catch (_: Throwable) {}
        }
        try { return cls.getField(name).get(instance) } catch (_: Throwable) {}
        try { return cls.getDeclaredField(name).apply { isAccessible = true }.get(instance) } catch (_: Throwable) {}
        return null
    }

    private fun readBoolProp(cls: Class<*>, instance: Any, name: String): Boolean? =
        readProp(cls, instance, name) as? Boolean

    override fun resolveOwner(internalName: String): String {
        val d = delegate ?: return internalName
        return try {
            val m = d.javaClass.getMethod("translate", String::class.java)
            val result = m.invoke(d, internalName) as String
            if (result != internalName) {
                Forensics.debug("resolveOwner: $internalName → $result")
            }
            result
        } catch (t: Throwable) {
            Forensics.warn("resolveOwner 失败: $internalName — ${t.javaClass.name}: ${t.message}")
            internalName
        }
    }

    override fun resolveMethod(owner: String, name: String, desc: String): Pair<String, String> {
        val d = delegate ?: return name to desc
        return try {
            // 走 owner 的实际类名（已转译）调用 Remapper.mapMethodName(owner, name, descriptor)
            val resolvedOwner = resolveOwner(owner)
            val m = mapMethodNameMethod(d) ?: return name to desc
            val mapped = m.invoke(d, resolvedOwner, name, desc) as? String ?: name
            if (mapped != name) {
                Forensics.debug("resolveMethod: $owner.$name$desc → $mapped")
            }
            mapped to desc
        } catch (t: Throwable) {
            Forensics.warn("resolveMethod 失败: $owner.$name$desc — ${t.javaClass.name}: ${t.message}")
            name to desc
        }
    }

    override fun resolveField(owner: String, name: String, desc: String): Pair<String, String> {
        val d = delegate ?: return name to desc
        return try {
            val resolvedOwner = resolveOwner(owner)
            val m = mapFieldNameMethod(d) ?: return name to desc
            val mapped = m.invoke(d, resolvedOwner, name, desc) as? String ?: name
            if (mapped != name) {
                Forensics.debug("resolveField: $owner.$name:$desc → $mapped")
            }
            mapped to desc
        } catch (t: Throwable) {
            Forensics.warn("resolveField 失败: $owner.$name:$desc — ${t.javaClass.name}: ${t.message}")
            name to desc
        }
    }

    @Volatile private var cachedMapMethodName: java.lang.reflect.Method? = null
    @Volatile private var cachedMapFieldName: java.lang.reflect.Method? = null

    private fun mapMethodNameMethod(d: Any): java.lang.reflect.Method? {
        cachedMapMethodName?.let { return it }
        return try {
            // org.objectweb.asm.commons.Remapper#mapMethodName(String, String, String): String
            val m = d.javaClass.getMethod("mapMethodName", String::class.java, String::class.java, String::class.java)
            m.isAccessible = true
            cachedMapMethodName = m
            m
        } catch (_: Throwable) { null }
    }

    private fun mapFieldNameMethod(d: Any): java.lang.reflect.Method? {
        cachedMapFieldName?.let { return it }
        return try {
            val m = d.javaClass.getMethod("mapFieldName", String::class.java, String::class.java, String::class.java)
            m.isAccessible = true
            cachedMapFieldName = m
            m
        } catch (_: Throwable) { null }
    }

    override fun isRemappableTarget(owner: String): Boolean {
        if (delegate == null) return false
        return owner.startsWith("net/minecraft/") ||
            owner.startsWith("com/mojang/") ||
            owner.startsWith("org/bukkit/craftbukkit/")
    }

    override fun envSignature(): String = "nms:$env"

    override fun name(): String = "TabooLibNmsResolver"
}
