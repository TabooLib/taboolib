package taboolib.module.kether.action.transform

import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * TabooLibKotlin
 * taboolib.module.kether.action.transform.ActionRange
 *
 * @author sky
 * @since 2021/1/30 9:26 下午
 */
class ActionRange(val from: Double, val to: Double, val step: Double = 0.0) : ScriptAction<List<Any>>() {

    override fun run(frame: ScriptFrame): CompletableFuture<List<Any>> {
        return if (step == 0.0) {
            CompletableFuture.completedFuture((from.toInt()..to.toInt()).toList())
        } else {
            val intStep = step.toInt().toDouble() == step
            val array = ArrayList<Any>()
            var i = from
            while (i <= to) {
                array.add(if (intStep) i.toInt() else i)
                i += step
            }
            CompletableFuture.completedFuture(array)
        }
    }

    internal object Parser {

        @KetherParser(["range"])
        fun parser() = scriptParser {
            val from = it.nextDouble()
            it.expect("to")
            val to = it.nextDouble()
            it.mark()
            val step = try {
                it.expect("step")
                it.nextDouble()
            } catch (ignored: Exception) {
                it.reset()
                0.0
            }
            ActionRange(from, to, step)
        }
    }
}