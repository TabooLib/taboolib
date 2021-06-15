package taboolib.common5

import taboolib.common5.util.Strings

fun String.replaceWithOrder(vararg args: Any) = Strings.replaceWithOrder(this, *args)!!