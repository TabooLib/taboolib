package taboolib.module.kether.action

import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import taboolib.common5.reflect.Reflex.Companion.reflex
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionImport : QuestAction<Void>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<Void> {
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ActionImport()"
    }

    companion object {

        @KetherParser(["import"])
        fun parser0() = ScriptParser.parser {
            it.reflex<MutableList<String>>("namespace")!!.add(it.nextToken())
            ActionImport()
        }

        @KetherParser(["release"])
        fun parser1() = ScriptParser.parser {
            it.reflex<MutableList<String>>("namespace")!!.remove(it.nextToken())
            ActionImport()
        }
    }
}