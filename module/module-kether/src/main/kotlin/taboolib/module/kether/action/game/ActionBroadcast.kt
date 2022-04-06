package taboolib.module.kether.action.game

import taboolib.common.platform.function.onlinePlayers
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionBroadcast(val message: ParsedAction<*>) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        return frame.newFrame(message).run<Any?>().thenAccept {
            onlinePlayers().forEach { p -> p.sendMessage(it.toString().trimIndent().replace("@player", p.name)) }
        }
    }

    internal object Parser {

        @KetherParser(["broadcast", "bc"])
        fun parser() = scriptParser {
            ActionBroadcast(it.next(ArgTypes.ACTION))
        }
    }
}