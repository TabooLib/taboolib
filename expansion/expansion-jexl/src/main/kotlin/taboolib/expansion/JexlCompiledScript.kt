package taboolib.expansion

/**
 * TabooLib
 * taboolib.expansion.JexlCompiledScript
 *
 * @author 坏黑
 * @since 2024/2/28 18:14
 */
interface JexlCompiledScript {

    /** 执行脚本 */
    fun eval(map: Map<String, Any?> = emptyMap()): Any?
}