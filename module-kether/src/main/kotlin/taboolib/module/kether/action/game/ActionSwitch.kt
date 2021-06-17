package taboolib.module.kether.action.game

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.common.platform.console
import taboolib.common.platform.getProxyPlayer
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import taboolib.module.kether.script
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionSwitch(val sender: ParsedAction<*>) : QuestAction<Void>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<Void> {
        return context.newFrame(sender).run<Any>().thenAccept {
            if (it.toString() == "console") {
                context.script().sender = console()
            } else {
                context.script().sender = getProxyPlayer(it.toString())
            }
        }
    }

    override fun toString(): String {
        return "ActionSwitch(sender=$sender)"
    }

    companion object {

        @KetherParser(["switch"])
        fun parser() = ScriptParser.parser {
            ActionSwitch(it.next(ArgTypes.ACTION))
        }
    }
}