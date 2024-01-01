@file:Isolated
@file:Suppress("NOTHING_TO_INLINE")

package taboolib.common5.util

import taboolib.common.Isolated
import taboolib.common5.Coerce
import taboolib.common5.TimeCycle

inline fun String.parseTimeCycle(): TimeCycle {
    val args = split(" ")
    return when (args[0]) {
        "day" -> TimeCycle(
            Coerce.toInteger(args[1]),
            Coerce.toInteger(args.getOrNull(2) ?: 0)
        )
        "week" -> TimeCycle(
            TimeCycle.Type.WEEK,
            Coerce.toInteger(args[1]),
            Coerce.toInteger(args.getOrNull(2) ?: 0),
            Coerce.toInteger(args.getOrNull(3) ?: 0)
        )
        "month" -> TimeCycle(
            TimeCycle.Type.MONTH,
            Coerce.toInteger(args[1]),
            Coerce.toInteger(args.getOrNull(2) ?: 0),
            Coerce.toInteger(args.getOrNull(3) ?: 0)
        )
        else -> TimeCycle(args[0].parseMillis())
    }.origin(this)
}