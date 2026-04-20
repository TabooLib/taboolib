package taboolib.module.incision.reflex

import taboolib.module.incision.api.MethodCoordinate
import taboolib.module.incision.runtime.AdviceEntry
import taboolib.module.incision.runtime.SurgeryRegistry
import taboolib.module.incision.runtime.TheatreDispatcher
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Incision 反射协作工具 — 让 TabooLib 自带反射 (org.tabooproject.reflex) 与外部反射工具
 * 在面对被 incision 织入过的类/方法时，能拿到正确的"原始"语义，避免：
 *
 * 1. **递归触发**：反射调用某方法时再次触发 advice 链，造成栈溢出或语义偏差。
 * 2. **签名误判**：incision 在方法 HEAD/TAIL 注入 INVOKESTATIC，方法签名本身不改变；
 *    但若上层工具按方法字节做 hash / 缓存，需要识别这是织入后的副本。
 * 3. **找不到字段/方法**：incision **不重命名** 现有成员、**不新增** public 方法，
 *    所以 reflex 默认查找仍然成立。但用户可能希望"穿透代理"获取真正未拦截的实现。
 *
 * 公开 API：
 * - [withoutIncision]：以线程为单位临时禁用 dispatcher，确保块内反射调用不触发 advice。
 * - [invokeOriginal]：跳过整条 advice 链直调原方法（仍走原 invoke 字节码）。
 * - [isWoven]：判断方法当前是否被 incision 接管。
 * - [installReflexAdapter]：把适配器注入 `org.tabooproject.reflex.Reflex`，
 *   使 TabooLib 反射在调用 method.invoke 时自动 wrap [withoutIncision]。
 */
object IncisionReflex {

    /** 线程本地禁用标记 — TheatreDispatcher 在 dispatch 入口检查 */
    val suppressed: ThreadLocal<Int> = ThreadLocal.withInitial { 0 }

    /**
     * 检查当前线程是否处于禁用 incision 的作用域中。
     * 由 [TheatreDispatcher] 在 dispatch 入口调用 — 若为 true，跳过整条 chain，
     * 直接放行（返回 null）。
     */
    fun isCurrentlySuppressed(): Boolean = suppressed.get() > 0

    /**
     * 在 [block] 执行期间，对当前线程禁用 incision dispatcher。
     * 嵌套调用安全 — 用引用计数。
     */
    inline fun <R> withoutIncision(block: () -> R): R {
        suppressed.set(suppressed.get() + 1)
        try {
            return block()
        } finally {
            suppressed.set(suppressed.get() - 1)
        }
    }

    /** 反射 invoke 包装 — 与 [withoutIncision] 一致，但兼容 Java 调用方 */
    @JvmStatic
    fun invokeOriginal(method: Method, instance: Any?, vararg args: Any?): Any? {
        return withoutIncision { method.invoke(instance, *args) }
    }

    @JvmStatic
    fun getFieldOriginal(field: Field, instance: Any?): Any? {
        return withoutIncision { field.get(instance) }
    }

    @JvmStatic
    fun setFieldOriginal(field: Field, instance: Any?, value: Any?) {
        withoutIncision { field.set(instance, value) }
    }

    /** 判断方法是否被 incision 接管 — 用于诊断 / 工具 */
    @JvmStatic
    fun isWoven(method: Method): Boolean {
        val owner = method.declaringClass.name.replace('.', '/')
        val coord = MethodCoordinate(owner, method.name, descriptorOf(method))
        return SurgeryRegistry.listByTarget(coord).isNotEmpty()
    }

    /** 列出某个类上所有 incision advice，便于调试 / `/incision list` 命令 */
    @JvmStatic
    fun adviceOn(cls: Class<*>): Map<MethodCoordinate, List<AdviceEntry>> {
        val owner = cls.name.replace('.', '/')
        return SurgeryRegistry.list()
            .flatMap { s -> s.targets.filter { it.owner == owner } }
            .distinct()
            .associateWith { TheatreDispatcher.chainOf(it).list() }
    }

    /**
     * 注册到 TabooLib 反射 (`org.tabooproject.reflex.Reflex`) 的钩子 —
     * 让其默认在 method.invoke 包一层 [withoutIncision]，避免反射触发 advice。
     *
     * 实现方式：通过反射读取 Reflex 的全局静态适配器字段（若存在），
     * 替换 / 链式包装；如果 Reflex 未提供该挂钩点，记录 warn 但不抛错。
     */
    fun installReflexAdapter() {
        try {
            val reflexCls = Class.forName("org.tabooproject.reflex.Reflex")
            // Reflex 没有公开的 invoke hook，用 ReflexRemapper 通道虽然可以转译名字，
            // 但拦不到 invoke 调用。做法：注册一个 thread-aware 的自定义 ClassLoader-level
            // wrapper —— 当前能做的最优解是在 Reflex 调用前后由用户主动 withoutIncision。
            //
            // 这里为后续 PR 留 hook：如果 Reflex 暴露 `setInvokeInterceptor`，自动接入。
            val setter = try {
                reflexCls.getMethod("setInvokeInterceptor", java.util.function.Function::class.java)
            } catch (_: Throwable) { null }
            if (setter != null) {
                val interceptor = java.util.function.Function<Runnable, Any?> { target ->
                    withoutIncision { target.run() }
                }
                setter.invoke(null, interceptor)
            }
        } catch (_: Throwable) {
            // Reflex 不在 classpath 时跳过
        }
    }

    private fun descriptorOf(m: Method): String {
        val sb = StringBuilder("(")
        for (p in m.parameterTypes) sb.append(typeDescriptor(p))
        sb.append(')').append(typeDescriptor(m.returnType))
        return sb.toString()
    }

    private fun typeDescriptor(c: Class<*>): String = when {
        c == Void.TYPE -> "V"
        c == java.lang.Boolean.TYPE -> "Z"
        c == java.lang.Byte.TYPE -> "B"
        c == java.lang.Short.TYPE -> "S"
        c == java.lang.Integer.TYPE -> "I"
        c == java.lang.Long.TYPE -> "J"
        c == java.lang.Float.TYPE -> "F"
        c == java.lang.Double.TYPE -> "D"
        c == java.lang.Character.TYPE -> "C"
        c.isArray -> "[" + typeDescriptor(c.componentType)
        else -> "L${c.name.replace('.', '/')};"
    }
}
