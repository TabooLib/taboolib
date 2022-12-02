package taboolib.module.kether.action.transform

import org.apache.commons.lang3.time.DateFormatUtils
import taboolib.common5.Coerce
import taboolib.common5.util.printed
import taboolib.module.kether.*
import taboolib.module.kether.ParserHolder.option
import kotlin.math.roundToInt

internal object Actions {

    @KetherParser(["array"])
    fun actionArray() = combinationParser {
        it.group(any().listOf()).apply(it) { array -> now { array } }
    }

    @KetherParser(["scale", "scaled"])
    fun actionScale() = combinationParser {
        it.group(double()).apply(it) { d -> now { Coerce.format(d) }}
    }

    @KetherParser(["round"])
    fun actionRound() = combinationParser {
        it.group(double()).apply(it) { d -> now { d.roundToInt() }}
    }

    @KetherParser(["split"])
    fun actionSplit() = combinationParser {
        it.group(text(), command("by", "with", then = text()).option()).apply(it) { t, s ->
            now { if (s != null) t.split(s.toRegex()) else t.toCharArray().map { c -> c.toString() } }
        }
    }

    @KetherParser(["format"])
    fun actionFormat() = combinationParser {
        it.group(long(), command("by", "with", then = text()).option()).apply(it) { t, s ->
            now { DateFormatUtils.format(t, s ?: "yyyy/MM/dd HH:mm") }
        }
    }

    @KetherParser(["printed"])
    fun actionPrinted() = combinationParser {
        it.group(text(), command("by", "with", then = text()).option()).apply(it) { t, s ->
            now { t.printed(s ?: "_") }
        }
    }

    @KetherParser(["check"])
    fun actionCheck() = combinationParser {
        it.group(any(), symbol(), any()).apply(it) { l, s, r ->
            val ct = CheckType.fromString(s)
            now { ct.check(l, r) }
        }
    }

    @KetherParser(["inline", "function"])
    fun actionFunction() = combinationParser {
        it.group(text()).apply(it) { f ->
            now { runKether(f) { KetherFunction.parse(f, sender = script().sender, vars = KetherShell.VariableMap(deepVars())) } }
        }
    }

    @KetherParser(["optional"])
    fun actionOptional() = combinationParser {
        it.group(any(), command("else", then = action()).option()).apply(it) { t, e ->
            future {
                when {
                    t != null -> completedFuture(t)
                    e != null -> run(e)
                    else -> completedFuture(null)
                }
            }
        }
    }

    @KetherParser(["range"])
    fun actionRange() = combinationParser {
        it.group(
            double(),
            command("to", then = double()),
            command("step", then = double()).option().defaultsTo(0.0)
        ).apply(it) { from, to, s ->
            now {
                if (s == 0.0) {
                    (from.toInt()..to.toInt()).toList()
                } else {
                    val intStep = s.toInt().toDouble() == s
                    val array = ArrayList<Any>()
                    var i = from
                    while (i <= to) {
                        array.add(if (intStep) i.toInt() else i)
                        i += s
                    }
                    array
                }
            }
        }
    }
}