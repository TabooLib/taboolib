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
        return frame.run(source).str { s ->
            try {
                KetherFunction.parse(s, sender = frame.script().sender, vars = KetherShell.VariableMap(vars))
            } catch (e: Exception) {
                e.printStackTrace()
                s
            }
        }
    }

    object Parser {

        @KetherParser(["inline", "function"])
        fun parser() = scriptParser {
            ActionFunction(it.next(ArgTypes.ACTION))
        }
    }
}