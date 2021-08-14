package taboolib.module.kether.action.transform

import taboolib.common5.Coerce
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
class ActionScale(val number: ParsedAction<*>) : ScriptAction<Double>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Double> {
        return frame.newFrame(number).run<Any>().thenApply {
            Coerce.format(Coerce.toDouble(it))
        }
    }

    internal object Parser {

        @KetherParser(["scale", "scaled"])
        fun parser() = scriptParser {
            ActionScale(it.next(ArgTypes.ACTION))
        }
    }
}