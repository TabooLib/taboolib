@file:Suppress("UNCHECKED_CAST")

package taboolib.module.kether.action

import taboolib.common.platform.function.warning
import taboolib.common.util.t
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

    fun getScriptProperty(obj: Any): Collection<ScriptProperty<*>> {
        // 根据类继承关系远近排序，优先选择最近的父类
        return Kether.registeredScriptProperty.filterKeys {
            it.isInstance(obj)
        }.map {
            it.key to it.value.values
        }.sortedWith { c1, c2 ->
            if (c1.first.isAssignableFrom(c2.first)) 1 else -1
        }.flatMap {
            it.second
        }
    }

    fun getScriptProperty(obj: Any, key: String): Any? {
        for (property in getScriptProperty(obj)) {
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
            frame.newFrame(instance).run<Any>().thenApply { instance ->
                if (instance == null) {
                    warning(
                        """
                            属性对象不能为空。
                            Property object must be not null.
                        """.t()
                    )
                    future.complete(null)
                }
                frame.newFrame(value).run<Any?>().thenAccept close@{ value ->
                    val propertyList = getScriptProperty(instance)
                    for (property in propertyList) {
                        val result = (property as ScriptProperty<Any>).write(instance, key, value)
                        if (result.isSuccessful) {
                            future.complete(null)
                            return@close
                        }
                    }
                    val prop = "${instance.javaClass.simpleName}[$key]"
                    warning(
                        """
                            $prop 尚未支持。
                            $prop not supported yet.
                        """.t()
                    )
                    future.complete(null)
                }
            }
            return future
        }
    }

    class Get(val instance: ParsedAction<*>, val key: String) : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            return frame.newFrame(instance).run<Any>().thenApply { instance ->
                if (instance == null) {
                    warning(
                        """
                            属性对象不能为空。
                            Property object must be not null.
                        """.t()
                    )
                    return@thenApply null
                }
                val propertyList = getScriptProperty(instance)
                for (property in propertyList) {
                    val result = (property as ScriptProperty<Any>).read(instance, key)
                    if (result.isSuccessful) {
                        return@thenApply result.value
                    }
                }
                val prop = "${instance.javaClass.simpleName}[$key]"
                warning(
                    """
                        $prop 尚未支持。
                        $prop not supported yet.
                    """.t()
                )
                return@thenApply null
            }
        }
    }
}