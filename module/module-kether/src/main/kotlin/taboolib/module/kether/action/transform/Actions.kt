package taboolib.module.kether.action.transform

import org.apache.commons.lang3.time.DateFormatUtils
import taboolib.common5.Coerce
import taboolib.common5.util.printed
import taboolib.module.kether.*
import kotlin.math.roundToInt

internal object Actions {

    /**
     * 格式化数字
     */
    @KetherParser(["scale", "scaled"])
    fun actionScale() = combinationParser {
        it.group(double()).apply(it) { d -> now { Coerce.format(d) }}
    }

    /**
     * 取整
     */
    @KetherParser(["round"])
    fun actionRound() = combinationParser {
        it.group(double()).apply(it) { d -> now { d.roundToInt() }}
    }

    /**
     * 拆分字符串
     */
    @KetherParser(["split"])
    fun actionSplit() = combinationParser {
        it.group(text(), command("by", "with", then = text()).option()).apply(it) { t, s ->
            now { if (s != null) t.split(s.toRegex()) else t.toCharArray().map { c -> c.toString() }.toMutableList() }
        }
    }

    /**
     * 格式化时间
     */
    @KetherParser(["format"])
    fun actionFormat() = combinationParser {
        it.group(long(), command("by", "with", then = text()).option()).apply(it) { t, s ->
            now { DateFormatUtils.format(t, s ?: "yyyy/MM/dd HH:mm") }
        }
    }

    /**
     * 将字符串转换为打字机效果
     */
    @KetherParser(["printed"])
    fun actionPrinted() = combinationParser {
        it.group(text(), command("by", "with", then = text()).option()).apply(it) { t, s ->
            now { t.printed(s ?: "_").toMutableList() }
        }
    }

    /**
     * 比较
     */
    @KetherParser(["check"])
    fun actionCheck() = combinationParser {
        it.group(any(), symbol(), any()).apply(it) { l, s, r ->
            val ct = CheckType.fromString(s)
            now { ct.check(l, r) }
        }
    }

    /**
     * 内联函数
     */
    @KetherParser(["inline", "function"])
    fun actionFunction() = combinationParser {
        it.group(text()).apply(it) { f ->
            now { runKether(f) { KetherFunction.parse(f, sender = script().sender, vars = KetherShell.VariableMap(deepVars())) } }
        }
    }

    /**
     * 可能为空的值
     */
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

    /**
     * 取一个范围内的数字
     */
    @KetherParser(["range"])
    fun actionRange() = combinationParser {
        it.group(
            double(),
            command("to", then = double()),
            command("step", then = double()).option().defaultsTo(0.0)
        ).apply(it) { from, to, s ->
            now {
                if (s == 0.0) {
                    (from.toInt()..to.toInt()).toMutableList()
                } else {
                    val intStep = s.toInt().toDouble() == s
                    val array = arrayListOf<Any>()
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