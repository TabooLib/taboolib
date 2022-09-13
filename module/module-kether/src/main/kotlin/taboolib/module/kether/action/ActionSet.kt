package taboolib.module.kether.action

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestContext
import taboolib.module.kether.KetherParser
import taboolib.module.kether.literalAction
import taboolib.module.kether.run
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
            return frame.run(action).thenAccept {
                frame.variables().set(key, it)
            }
        }
    }

    object Parser {

        /**
         * set xx to xx
         * set property xx from xx to xx
         */
        @KetherParser(["set"])
        fun parser0() = scriptParser {
            it.mark()
            val token = it.nextToken()
            if (token.isNotEmpty() && token[token.length - 1] == ']' && token.indexOf('[') in 1 until token.length) {
                it.reset()
                val action = it.nextParsedAction().action as ActionProperty.Get
                it.mark()
                try {
                    it.expect("to")
                    ActionProperty.Set(action.instance, action.key, it.nextParsedAction())
                } catch (ex: Exception) {
                    it.reset()
                    ActionProperty.Set(action.instance, action.key, literalAction(it.nextToken()))
                }
            } else if (token == "property") {
                it.mark()
                try {
                    val property = it.nextToken()
                    it.expect("from")
                    val source = it.nextParsedAction()
                    it.expect("to")
                    ActionProperty.Set(source, property, it.nextParsedAction())
                } catch (ex: Exception) {
                    it.reset()
                    it.mark()
                    try {
                        it.expect("to")
                        ForAction(token, it.nextParsedAction())
                    } catch (ex: Exception) {
                        it.reset()
                        ForConstant(token, it.nextToken())
                    }
                }
            } else {
                it.mark()
                try {
                    it.expect("to")
                    ForAction(token, it.nextParsedAction())
                } catch (ex: Exception) {
                    it.reset()
                    ForConstant(token, it.nextToken())
                }
            }
        }
    }
}