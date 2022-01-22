package taboolib.module.kether.action

import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionImport : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        return CompletableFuture.completedFuture(null)
    }

    internal object Parser {

        @KetherParser(["import"])
        fun parser0() = scriptParser {
            it.getProperty<MutableList<String>>("namespace")!!.add(it.nextToken())
            ActionImport()
        }

        @KetherParser(["release"])
        fun parser1() = scriptParser {
            it.getProperty<MutableList<String>>("namespace")!!.remove(it.nextToken())
            ActionImport()
        }
    }
}