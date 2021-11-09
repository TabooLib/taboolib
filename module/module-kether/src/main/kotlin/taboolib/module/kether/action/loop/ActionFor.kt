package taboolib.module.kether.action.loop

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionFor(val key: String, val values: ParsedAction<*>, val action: ParsedAction<*>) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        frame.newFrame(values).run<Any>().thenApply {
            fun process(cur: Int, i: List<Any>) {
                if (cur < i.size) {
                    val el = i[cur]
                    if (el is Map.Entry<*, *>) {
                        frame.variables()["$key-key"] = el.key
                        frame.variables()["$key-value"] = el.value
                    }
                    frame.variables()[key] = el
                    frame.newFrame(action).run<Any>().thenApply {
                        if (frame.script().breakLoop) {
                            frame.script().breakLoop = false
                            frame.variables().also { v ->
                                v.remove(key)
                                v.remove("$key-key")
                                v.remove("$key-value")
                            }
                            future.complete(null)
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
                    future.complete(null)
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

        /**
         * for i in players then {  }
         * for i in range 1 to 10 then {  }
         */
        @KetherParser(["for"])
        fun parser() = scriptParser {
            ActionFor(it.nextToken(), it.run {
                expect("in")
                next(ArgTypes.ACTION)
            }, it.run {
                expect("then")
                next(ArgTypes.ACTION)
            })
        }
    }
}