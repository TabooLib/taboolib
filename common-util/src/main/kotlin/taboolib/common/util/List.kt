@file:Isolated

package taboolib.common.util

import taboolib.common.Isolated

fun Any.asList() =
    if (this is Collection<*>) map { it.toString() } else listOf(toString())