package taboolib.module.kether

import taboolib.common.OpenResult

/**
 * TabooLib
 * taboolib.module.kether.ScriptProperty
 *
 * @author sky
 * @since 2021/8/9 12:24 上午
 */
abstract class ScriptProperty(val id: String) {

    abstract fun read(instance: Any, key: String): OpenResult

    abstract fun write(instance: Any, key: String, value: Any?): OpenResult
}