package taboolib.module.kether.action

import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionTerminate : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        ScriptService.terminateQuest(frame.script())
        return CompletableFuture.completedFuture(null)
    }

    internal object Parser {

        @KetherParser(["exit", "stop", "terminate"])
        fun parser() = scriptParser {
            ActionTerminate()
        }
    }
}