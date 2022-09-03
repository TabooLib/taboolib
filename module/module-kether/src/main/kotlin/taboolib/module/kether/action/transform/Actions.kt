package taboolib.module.kether.action.transform

import taboolib.common5.Coerce
import taboolib.module.kether.*
import kotlin.math.roundToLong

object Actions {

    @KetherParser(["scale", "scaled"])
    fun actionScale() = scriptParser {
        val num = it.nextParsedAction()
        actionTake { run(num).double { d -> Coerce.format(d) } }
    }

    @KetherParser(["round"])
    fun actionRound() = scriptParser {
        val num = it.nextParsedAction()
        actionTake { run(num).double { d -> d.roundToLong() } }
    }

    @KetherParser(["split"])
    fun actionSplit() = scriptParser {
        val str = it.nextParsedAction()
        val regex = try {
            it.mark()
            it.expects("by", "with")
            it.nextParsedAction()
        } catch (ignored: Exception) {
            it.reset()
            null
        }
        actionTake {
            run(str).str { s ->
                if (regex != null) {
                    run(regex).str { r -> s.split(r.toRegex()) }.join()
                } else {
                    s.toCharArray().map { c -> c.toString() }
                }
            }
        }
    }
}