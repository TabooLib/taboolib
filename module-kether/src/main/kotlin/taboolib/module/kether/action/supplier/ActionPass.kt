package taboolib.module.kether.action.supplier

import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture


/**
 * @author IzzelAliz
 */
class ActionPass : ScriptAction<String>() {

    override fun run(frame: ScriptFrame): CompletableFuture<String> {
        return CompletableFuture.completedFuture("")
    }

    override fun toString(): String {
        return "ActionPass()"
    }

    companion object {

        @KetherParser(["pass"])
        fun parser() = scriptParser {
            ActionPass()
        }
    }
}