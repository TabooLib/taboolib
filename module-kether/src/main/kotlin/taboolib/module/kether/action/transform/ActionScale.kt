package taboolib.module.kether.action.transform

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.common5.Coerce
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

    override fun toString(): String {
        return "ActionScale(number=$number)"
    }

    companion object {

        @KetherParser(["scale", "scaled"])
        fun parser() = scriptParser {
            ActionScale(it.next(ArgTypes.ACTION))
        }
    }
}