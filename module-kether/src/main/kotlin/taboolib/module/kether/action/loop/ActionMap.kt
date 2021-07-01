package taboolib.module.kether.action.loop

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.loader.types.ArgTypes
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
                    frame.variables()[key] = i[cur]
                    frame.newFrame(action).run<Any>().thenApply { map ->
                        if (map != null) {
                            result += map
                        }
                        if (frame.script().breakLoop) {
                            frame.script().breakLoop = false
                            frame.variables().remove(key)
                            future.complete(result)
                        } else {
                            process(cur + 1, i)
                        }
                    }
                } else {
                    frame.variables().remove(key)
                    future.complete(result)
                }
            }
            when (it) {
                is Collection<*> -> {
                    process(0, it.map { i -> i as Any }.toList())
                }
                is Array<*> -> {
                    process(0, it.map { i -> i as Any }.toList())
                }
                else -> {
                    process(0, listOf(it))
                }
            }
        }
        return future
    }

    companion object {

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