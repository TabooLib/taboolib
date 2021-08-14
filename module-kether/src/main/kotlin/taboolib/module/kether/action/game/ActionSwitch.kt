package taboolib.module.kether.action.game

import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionSwitch(val sender: ParsedAction<*>) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        return frame.newFrame(sender).run<Any>().thenAccept {
            if (it.toString() == "console") {
                frame.script().sender = console()
            } else {
                frame.script().sender = getProxyPlayer(it.toString())
            }
        }
    }

    internal object Parser {

        @KetherParser(["switch"])
        fun parser() = scriptParser {
            ActionSwitch(it.next(ArgTypes.ACTION))
        }
    }
}