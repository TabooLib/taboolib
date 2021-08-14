package taboolib.module.kether.action.game

import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionActionBar(val message: ParsedAction<*>) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        return frame.newFrame(message).run<Any>().thenAccept {
            val viewer = frame.script().sender as? ProxyPlayer ?: error("No player selected.")
            viewer.sendActionBar(it.toString().trimIndent().replace("@sender", viewer.name))
        }
    }

    internal object Parser {

        @KetherParser(["actionbar"])
        fun parser() = scriptParser {
            ActionActionBar(it.next(ArgTypes.ACTION))
        }
    }
}