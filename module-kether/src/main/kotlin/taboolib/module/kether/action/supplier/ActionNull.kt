package taboolib.module.kether.action.supplier

import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture


/**
 * @author IzzelAliz
 */
class ActionNull : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ActionNull()"
    }

    companion object {

        @KetherParser(["null"])
        fun parser() = scriptParser {
            ActionNull()
        }
    }
}