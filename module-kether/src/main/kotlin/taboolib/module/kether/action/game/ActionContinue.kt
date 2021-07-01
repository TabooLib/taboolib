package taboolib.module.kether.action.game

import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture


/**
 * @author IzzelAliz
 */
class ActionContinue : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        val s = frame.context() as ScriptContext
        s.listener?.complete(null)
        s.listener = null
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ActionContinue()"
    }

    companion object {

        @KetherParser(["continue"])
        fun parser() = scriptParser {
            ActionContinue()
        }
    }
}