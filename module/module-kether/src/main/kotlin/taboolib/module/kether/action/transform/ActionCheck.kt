package taboolib.module.kether.action.transform

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
class ActionCheck(val left: ParsedAction<*>, val right: ParsedAction<*>, val checkType: CheckType) : ScriptAction<Boolean>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
        return CompletableFuture<Boolean>().also { future ->
            frame.newFrame(left).run<Any?>().thenAccept { left ->
                frame.newFrame(right).run<Any?>().thenAccept { right ->
                    future.complete(checkType.check(left, right))
                }
            }
        }
    }

    companion object {

        @KetherParser(["check"])
        fun parser() = scriptParser {
            val left = it.next(ArgTypes.ACTION)
            val symbol = CheckType.fromString(it.nextToken())
            val right = it.next(ArgTypes.ACTION)
            ActionCheck(left, right, symbol)
        }
    }
}