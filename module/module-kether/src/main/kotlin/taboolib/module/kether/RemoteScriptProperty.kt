package taboolib.module.kether

import taboolib.common.OpenContainer
import taboolib.common.OpenResult
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * TabooLib
 * taboolib.module.kether.RemoteScriptProperty
 *
 * @author sky
 * @since 2021/8/12 8:37 下午
 */
class RemoteScriptProperty(val remote: OpenContainer, val source: Any, id: String) : ScriptProperty<Any>(id) {

    override fun read(instance: Any, key: String): OpenResult {
        return OpenResult.deserialize(source.invokeMethod("read", instance, key))
    }

    override fun write(instance: Any, key: String, value: Any?): OpenResult {
        return OpenResult.deserialize(source.invokeMethod("write", instance, key, value))
    }
}