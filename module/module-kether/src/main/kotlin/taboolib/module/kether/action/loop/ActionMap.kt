package taboolib.module.kether.action.loop

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionMap(val key: String, val values: ParsedAction<*>, val action: ParsedAction<*>) : ScriptAction<List<Any>>() {

    override fun run(frame: ScriptFrame): CompletableFuture<List<Any>> {
        val future = CompletableFuture<List<Any>>()
        val result = ArrayList<Any>()
        frame.newFrame(values).run<Any>().thenApply {
            fun process(cur: Int, i: List<Any>) {
                if (cur < i.size) {
                    val el = i[cur]
                    if (el is Map.Entry<*, *>) {
                        frame.variables()["$key-key"] = el.key
                        frame.variables()["$key-value"] = el.value
                    }
                    frame.variables()[key] = el
                    frame.newFrame(action).run<Any>().thenApply { map ->
                        if (map != null) {
                            result += map
                        }
                        if (frame.script().breakLoop) {
                            frame.script().breakLoop = false
                            frame.variables().also { v ->
                                v.remove(key)
                                v.remove("$key-key")
                                v.remove("$key-value")
                            }
                            future.complete(result)
                        } else {
                            process(cur + 1, i)
                        }
                    }
                } else {
                    frame.variables().also { v ->
                        v.remove(key)
                        v.remove("$key-key")
                        v.remove("$key-value")
                    }
                    future.complete(result)
                }
            }
            when (it) {
                is Collection<*> -> process(0, it.map { i -> i as Any }.toList())
                is Array<*> -> process(0, it.map { i -> i as Any }.toList())
                is Map<*, *> -> process(0, it.entries.toList())
                else -> process(0, listOf(it))
            }
        }
        return future
    }

    internal object Parser {

        @KetherParser(["map"])
        fun parser() = scriptParser {
            ActionMap(it.nextToken(), it.run {
                expect("in")
                next(ArgTypes.ACTION)
            }, it.run {
                expect("with")
                next(ArgTypes.ACTION)
            })
        }
    }
}