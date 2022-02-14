package taboolib.common.io

import taboolib.common.TabooLib

val groupId: String?
    get() = if (TabooLib::class.java.name.startsWith(taboolibId)) null else TabooLib::class.java.name.substringBefore(".$taboolibId")

val taboolibId: String
    get() = charArrayOf('t', 'a', 'b', 'o', 'o', 'l', 'i', 'b').concatToString()

val taboolibPath: String?
    get() = if (groupId == null) null else "$groupId.$taboolibId"

val Class<*>.groupId: String?
    get() = if (name.startsWith(taboolibId)) null else name.substringBefore(".$taboolibId")

val Class<*>.tabooLibPath: String
    get() = if (groupId == null) taboolibId else "$groupId.$taboolibId"