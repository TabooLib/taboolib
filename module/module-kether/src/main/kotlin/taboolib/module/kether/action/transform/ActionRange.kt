package taboolib.module.kether.action.transform

import taboolib.common5.Coerce
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * TabooLibKotlin
 * taboolib.module.kether.action.transform.ActionRange
 *
 * @author sky
 * @since 2021/1/30 9:26 下午
 */
class ActionRange(val from: ParsedAction<*>, val to: ParsedAction<*>, val step: ParsedAction<*> = literalAction(0)) : ScriptAction<List<Any>>() {

    override fun run(frame: ScriptFrame): CompletableFuture<List<Any>> {
        return frame.newFrame(step).run<Any>().thenApply { s ->
            val step = Coerce.toDouble(s)
            frame.newFrame(from).run<Any>().thenApply { from ->
                frame.newFrame(to).run<Any>().thenApply { to ->
                    if (step == 0.0) {
                        (Coerce.toInteger(from)..Coerce.toInteger(to)).toList()
                    } else {
                        val intStep = step.toInt().toDouble() == step
                        val array = ArrayList<Any>()
                        var i = Coerce.toDouble(from)
                        val t = Coerce.toDouble(to)
                        while (i <= t) {
                            array.add(if (intStep) i.toInt() else i)
                            i += step
                        }
                        array
                    }
                }.join()
            }.join()
        }
    }

    internal object Parser {

        @KetherParser(["range"])
        fun parser() = scriptParser {
            val from = it.nextParsedAction()
            it.expect("to")
            val to = it.nextParsedAction()
            it.mark()
            val step = try {
                it.expect("step")
                it.nextParsedAction()
            } catch (ignored: Exception) {
                it.reset()
                literalAction(0)
            }
            ActionRange(from, to, step)
        }
    }
}