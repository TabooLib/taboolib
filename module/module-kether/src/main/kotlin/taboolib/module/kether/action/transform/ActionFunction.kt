package taboolib.module.kether.action.transform

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * function "your name is {{player name}}"
 * @author IzzelAliz
 */
class ActionFunction(val source: ParsedAction<*>) : ScriptAction<String>() {

    override fun run(frame: ScriptFrame): CompletableFuture<String> {
        val vars = frame.deepVars()
        return frame.newFrame(source).run<Any>().thenApply {
            try {
                KetherFunction.parse(it.toString().trimIndent()) {
                    vars.forEach { (k, v) -> rootFrame().variables().set(k, v) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                it.toString()
            }
        }
    }

    internal object Parser {

        @KetherParser(["inline", "function"])
        fun parser() = scriptParser {
            ActionFunction(it.next(ArgTypes.ACTION))
        }
    }
}