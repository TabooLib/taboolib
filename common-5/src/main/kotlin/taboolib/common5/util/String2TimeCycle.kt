@file:Isolated

package taboolib.common5.util

import taboolib.common.io.Isolated
import taboolib.common5.Coerce
import taboolib.common5.TimeCycle
import taboolib.common5.TimeCycleUnit

fun String.parseTimeCycle(): TimeCycle {
    val args = split(" ")
    return when (args[0]) {
        "day" -> TimeCycle(
            Coerce.toInteger(args[1]),
            Coerce.toInteger(args.getOrNull(2) ?: 0)
        )
        "week" -> TimeCycle(
            TimeCycleUnit.WEEK,
            Coerce.toInteger(args[1]),
            Coerce.toInteger(args.getOrNull(2) ?: 0),
            Coerce.toInteger(args.getOrNull(3) ?: 0)
        )
        "month" -> TimeCycle(
            TimeCycleUnit.MONTH,
            Coerce.toInteger(args[1]),
            Coerce.toInteger(args.getOrNull(2) ?: 0),
            Coerce.toInteger(args.getOrNull(3) ?: 0)
        )
        else -> TimeCycle(args[0])
    }.origin(this)
}