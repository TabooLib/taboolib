package taboolib.expansion

import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.JexlEngine
import org.apache.commons.jexl3.MapContext
import taboolib.common.util.unsafeLazy

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

    internal val jexlEngine: JexlEngine by unsafeLazy { jexlBuilder.create() }

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
        jexlBuilder.antish(flag)
        return this
    }

    /** 设置严格模式 */
    fun strict(flag: Boolean): JexlCompiler {
        jexlBuilder.strict(flag)
        return this
    }

    /** 设置静默模式 */
    fun silent(flag: Boolean): JexlCompiler {
        jexlBuilder.silent(flag)
        return this
    }

    /** 设置安全模式 */
    fun safe(flag: Boolean): JexlCompiler {
        jexlBuilder.safe(flag)
        return this
    }

    /** 设置调试模式 */
    fun debug(flag: Boolean): JexlCompiler {
        jexlBuilder.debug(flag)
        return this
    }

    /** 设置缓存大小 */
    fun cache(size: Int): JexlCompiler {
        jexlBuilder.cache(size)
        return this
    }

    /** 设置收集模式 */
    fun collectMode(mode: Int): JexlCompiler {
        jexlBuilder.collectMode(mode)
        return this
    }

    /** 设置是否收集所有变量 */
    fun collectAll(flag: Boolean): JexlCompiler {
        jexlBuilder.collectAll(flag)
        return this
    }

    /** 设置缓存阈值 */
    fun cacheThreshold(size: Int): JexlCompiler {
        jexlBuilder.cacheThreshold(size)
        return this
    }

    /** 设置堆栈大小 */
    fun stackOverflow(size: Int): JexlCompiler {
        jexlBuilder.stackOverflow(size)
        return this
    }

    /** 设置命名空间 */
    fun namespace(namespace: Map<String, Any>): JexlCompiler {
        jexlBuilder.namespaces(namespace)
        return this
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

    companion object {

        fun new() = JexlCompiler()
    }
}