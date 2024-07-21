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
        .cache(256)

    internal val jexlEngine: JexlEngine by unsafeLazy { jexlBuilder.create() }

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