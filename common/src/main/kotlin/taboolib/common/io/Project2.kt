@file:Isolated
package taboolib.common.io

import taboolib.common.Isolated

inline val groupId: String
    get() = "taboolib".substring(0, "taboolib".length - 9)

inline val taboolibId: String
    get() = charArrayOf('t', 'a', 'b', 'o', 'o', 'l', 'i', 'b').concatToString()

inline val taboolibPath: String
    get() = groupId + taboolibId

inline val Class<*>.groupId: String
    get() = name.substringBefore(taboolibId).dropLast(1)