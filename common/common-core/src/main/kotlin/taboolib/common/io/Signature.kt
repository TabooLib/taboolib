@file:Isolated

package taboolib.common.io

import taboolib.common.Isolated
import taboolib.common.TabooLib

inline val groupId: String?
    get() = if (TabooLib::class.java.name.startsWith(taboolibId)) null else TabooLib::class.java.name.substringBefore(".$taboolibId")

inline val taboolibId: String
    get() = charArrayOf('t', 'a', 'b', 'o', 'o', 'l', 'i', 'b').concatToString()

inline val taboolibPath: String?
    get() = if (groupId == null) null else "$groupId.$taboolibId"

inline val Class<*>.groupId: String?
    get() = if (name.startsWith(taboolibId)) null else name.substringBefore(".$taboolibId")

inline val Class<*>.tabooLibPath: String
    get() = if (groupId == null) taboolibId else "$groupId.$taboolibId"