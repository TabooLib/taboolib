package taboolib.module.kether.action

import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionTerminate : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        ScriptService.terminateQuest(frame.context() as ScriptContext)
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ActionTerminate()"
    }

    companion object {

        @KetherParser(["exit", "stop", "terminate"])
        fun parser() = scriptParser {
            ActionTerminate()
        }
    }
}