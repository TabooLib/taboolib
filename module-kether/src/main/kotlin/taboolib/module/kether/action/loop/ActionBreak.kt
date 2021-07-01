package taboolib.module.kether.action.loop

import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionBreak : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        frame.script().breakLoop = true
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ActionBreak()"
    }

    companion object {

        @KetherParser(["break"])
        fun parser() = scriptParser {
            ActionBreak()
        }
    }
}