package taboolib.module.kether.action

import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestActionParser
import taboolib.library.kether.QuestContext
import java.util.concurrent.CompletableFuture

class ActionLiteral<T> : QuestAction<T> {

    val value: Any

    var isMisspelled = false
        private set

    constructor(value: Any) {
        this.value = value
    }

    constructor(value: String) {
        this.value = value
    }

    constructor(value: String, misspelled: Boolean) {
        this.value = value
        isMisspelled = misspelled
    }

    @Suppress("UNCHECKED_CAST")
    override fun process(frame: QuestContext.Frame): CompletableFuture<T> {
        return CompletableFuture.completedFuture(value as T)
    }

    companion object {

        fun parser(): QuestActionParser {
            return QuestActionParser.of { reader -> ActionLiteral<Any?>(reader.nextToken()) }
        }
    }
}