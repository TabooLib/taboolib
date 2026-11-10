package taboolib.expansion

import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.JexlEngine
import org.apache.commons.jexl3.MapContext

/**
 * TabooLib
 * taboolib.expansion.JexlCompiler
 *
 * @author 坏黑
 * @since 2024/2/28 18:11
 */
class JexlCompiler {

    internal val jexlBuilder: JexlBuilder = JexlBuilder()
        .strict(false)
        .cache(256)          // 启用缓存
        .cacheThreshold(64)  // 设置合适的缓存阈值
        .collectMode(0)      // 如果不需要变量收集，关闭它

    private val engineLock = Any()

    @Volatile
    private var currentEngine: JexlEngine? = null

    internal val jexlEngine: JexlEngine
        get() {
            currentEngine?.let { return it }
            return synchronized(engineLock) {
                currentEngine ?: jexlBuilder.create().also { currentEngine = it }
            }
        }

    /**
     * 是否启用 Ant 风格模式
     * Ant 风格（Antish）指的是支持类似 Apache Ant 中的属性访问语法。
     * 点号（.）和中括号（[]）可以互换使用：
     * 例如，${a.b.c} 等价于 ${a['b']['c']}
     *
     * ```
     * // 假设有一个对象 user
     * user.name  // 标准访问方式
     * user['name']  // Ant 风格，与上面等价
     *
     * // 对于多层属性
     * user.address.city  // 标准访问方式
     * user['address'].city  // Ant 风格
     * user['address']['city']  // Ant 风格
     * ```
     *
     * 对于简单表达式：影响很小，通常可以忽略
     * 对于复杂表达式：可能有 5-10% 的性能损耗
     * 在高频调用场景：影响会更明显
     */
    fun antish(flag: Boolean): JexlCompiler {
        return configure { antish(flag) }
    }

    /** 设置严格模式 */
    fun strict(flag: Boolean): JexlCompiler {
        return configure { strict(flag) }
    }

    /** 设置静默模式 */
    fun silent(flag: Boolean): JexlCompiler {
        return configure { silent(flag) }
    }

    /** 设置安全模式 */
    fun safe(flag: Boolean): JexlCompiler {
        return configure { safe(flag) }
    }

    /** 设置调试模式 */
    fun debug(flag: Boolean): JexlCompiler {
        return configure { debug(flag) }
    }

    /** 设置缓存大小 */
    fun cache(size: Int): JexlCompiler {
        return configure { cache(size) }
    }

    /** 设置收集模式 */
    fun collectMode(mode: Int): JexlCompiler {
        return configure { collectMode(mode) }
    }

    /** 设置是否收集所有变量 */
    fun collectAll(flag: Boolean): JexlCompiler {
        return configure { collectAll(flag) }
    }

    /** 设置缓存阈值 */
    fun cacheThreshold(size: Int): JexlCompiler {
        return configure { cacheThreshold(size) }
    }

    /** 设置堆栈大小 */
    fun stackOverflow(size: Int): JexlCompiler {
        return configure { stackOverflow(size) }
    }

    /** 设置命名空间 */
    fun namespace(namespace: Map<String, Any>): JexlCompiler {
        return configure { namespaces(namespace) }
    }

    /** 编译为脚本 */
    fun compileToScript(script: String): JexlCompiledScript {
        val jexlScript = jexlEngine.createScript(script)
        return object : JexlCompiledScript {

            override fun eval(map: Map<String, Any?>): Any? {
                return jexlScript.execute(MapContext(map))
            }
        }
    }

    /** 编译为表达式 */
    fun compileToExpression(script: String): JexlCompiledScript {
        val jexlExpression = jexlEngine.createExpression(script)
        return object : JexlCompiledScript {

            override fun eval(map: Map<String, Any?>): Any? {
                return jexlExpression.evaluate(MapContext(map))
            }
        }
    }

    private fun configure(configureBuilder: JexlBuilder.() -> Unit): JexlCompiler {
        synchronized(engineLock) {
            jexlBuilder.configureBuilder()
            currentEngine = null
        }
        return this
    }

    companion object {

        fun new() = JexlCompiler()
    }
}
