package taboolib.module.kether.action

import taboolib.library.kether.actions.LiteralAction
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestContext
import taboolib.library.kether.ArgTypes
import taboolib.module.kether.KetherParser
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

class ActionSet {

    class ForConstant(val key: String, val value: String?) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            if (value == null || value == "null") {
                frame.variables()[key] = null
            } else {
                frame.variables()[key] = value
            }
            return CompletableFuture.completedFuture(null)
        }
    }

    class ForAction(val key: String, val action: ParsedAction<*>) : QuestAction<Void>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
            frame.newFrame(action).run<Any?>().thenAccept {
                frame.variables().set(key, it)
            }
            return CompletableFuture.completedFuture(null)
        }
    }

    internal object Parser {

        @KetherParser(["set"])
        fun parser0() = scriptParser {
            it.mark()
            val token = it.nextToken()
            if (token.isNotEmpty() && token[token.length - 1] == ']' && token.indexOf('[') in 1 until token.length) {
                it.reset()
                val action = it.next(ArgTypes.ACTION).action as ActionProperty.Get
                it.mark()
                try {
                    it.expect("to")
                    ActionProperty.Set(action.instance, action.key, it.next(ArgTypes.ACTION))
                } catch (ex: Exception) {
                    it.reset()
                    ActionProperty.Set(action.instance, action.key, ParsedAction(LiteralAction<String>(it.nextToken())))
                }
            } else {
                it.mark()
                try {
                    it.expect("to")
                    ForAction(token, it.next(ArgTypes.ACTION))
                } catch (ex: Exception) {
                    it.reset()
                    ForConstant(token, it.nextToken())
                }
            }
        }
    }
}