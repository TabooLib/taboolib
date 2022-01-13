@file:Suppress("UNCHECKED_CAST")

package taboolib.module.kether.action

import taboolib.library.kether.ParsedAction
import taboolib.module.kether.Kether
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.ScriptProperty
import java.util.concurrent.CompletableFuture

/**
 * TabooLib
 * taboolib.module.kether.action.ActionProperty
 *
 * @author sky
 * @since 2021/7/26 3:03 下午
 */
object ActionProperty {

    fun getScriptProperty(obj: Any): Collection<ScriptProperty<*>>? {
        return Kether.registeredScriptProperty.entries.firstOrNull { it.key.isInstance(obj) }?.value?.values
    }

    fun getScriptProperty(obj: Any, key: String): Any? {
        for (property in getScriptProperty(obj) ?: return null) {
            val result = (property as ScriptProperty<Any>).read(obj, key)
            if (result.isSuccessful) {
                return result.value
            }
        }
        return null
    }

    class Set(val instance: ParsedAction<*>, val key: String, val value: ParsedAction<*>) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            val future = CompletableFuture<Void>()
            frame.newFrame(instance).run<Any>().thenAccept { instance ->
                if (instance == null) {
                    error("Property object must be not null.")
                }
                frame.newFrame(value).run<Any?>().thenAccept close@{ value ->
                    val propertyList = getScriptProperty(instance) ?: error("${instance.javaClass.simpleName}[$key] not supported yet.")
                    for (property in propertyList) {
                        val result = (property as ScriptProperty<Any>).write(instance, key, value)
                        if (result.isSuccessful) {
                            future.complete(null)
                            return@close
                        }
                    }
                    error("${instance.javaClass.simpleName}[$key] not supported yet.")
                }
            }
            return future
        }
    }

    class Get(val instance: ParsedAction<*>, val key: String) : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            return frame.newFrame(instance).run<Any>().thenApply {
                if (it == null) {
                    error("Property object must be not null.")
                }
                val propertyList = getScriptProperty(it) ?: error("${it.javaClass.simpleName}[$key] not supported yet.")
                for (property in propertyList) {
                    val result = (property as ScriptProperty<Any>).read(it, key)
                    if (result.isSuccessful) {
                        return@thenApply result.value
                    }
                }
                error("${it.javaClass.simpleName}[$key] not supported yet.")
            }
        }
    }
}