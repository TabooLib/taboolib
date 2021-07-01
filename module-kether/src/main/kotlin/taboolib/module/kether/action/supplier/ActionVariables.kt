package taboolib.module.kether.action.supplier

import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionVariables : ScriptAction<List<String>>() {

    override fun run(frame: ScriptFrame): CompletableFuture<List<String>> {
        return CompletableFuture.completedFuture(frame.deepVars().keys.toList())
    }

    override fun toString(): String {
        return "ActionVariables()"
    }

    companion object {

        @KetherParser(["vars", "variables"])
        fun parser() = scriptParser {
            ActionVariables()
        }
    }
}