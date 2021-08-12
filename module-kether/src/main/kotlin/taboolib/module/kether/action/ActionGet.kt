package taboolib.module.kether.action

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestContext
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

class ActionGet<T>(val key: String) : QuestAction<T>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<T> {
        return CompletableFuture.completedFuture(frame.variables().get<T?>(key).orElse(null))
    }

    internal object Parser {

        /**
         * get xx
         * get property xx from xx
         */
        @KetherParser(["get"])
        fun parser() = scriptParser {
            it.switch {
                case("property") {
                    val property = it.nextToken()
                    it.expects("from", "in")
                    ActionProperty.Get(it.next(ArgTypes.ACTION), property)
                }
                other {
                    val key = it.nextToken()
                    actionNow {
                        variables().get<Any?>(key).orElse(null)
                    }
                }
            }
        }
    }
}