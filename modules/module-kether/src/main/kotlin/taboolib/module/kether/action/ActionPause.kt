package taboolib.module.kether.action

import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionPause : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        return CompletableFuture<Void>()
    }

    internal object Parser {

        @KetherParser(["pause"])
        fun parser() = scriptParser {
            ActionPause()
        }
    }
}