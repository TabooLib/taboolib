@file:Isolated
package taboolib.common.platform.command

import taboolib.common.Isolated
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.onlinePlayers

@Deprecated("设计过于傻逼,令人智熄", ReplaceWith("int(id)"))
fun <T> CommandContext<T>.int(offset: Int): Int {
    return argument(offset).toInt()
}

@Deprecated("设计过于傻逼,令人智熄", ReplaceWith("int(id)"))
fun <T> CommandContext<T>.intAt(index: Int): Int {
    return get(index).toInt()
}

@Deprecated("设计过于傻逼,令人智熄", ReplaceWith("double(id)"))
fun <T> CommandContext<T>.double(offset: Int): Double {
    return argument(offset).toDouble()
}

@Deprecated("设计过于傻逼,令人智熄", ReplaceWith("double(id)"))
fun <T> CommandContext<T>.doubleAt(index: Int): Double {
    return get(index).toDouble()
}

@Deprecated("设计过于傻逼,令人智熄", ReplaceWith("bool(id)"))
fun <T> CommandContext<T>.bool(offset: Int): Boolean {
    return argument(offset).toBooleanStrict()
}

@Deprecated("设计过于傻逼,令人智熄", ReplaceWith("bool(id)"))
fun <T> CommandContext<T>.boolAt(index: Int): Boolean {
    return get(index).toBoolean()
}

@Deprecated("设计过于傻逼,令人智熄", ReplaceWith("player(id)"))
fun <T> CommandContext<T>.player(offset: Int): ProxyPlayer {
    return onlinePlayers().first { it.name == argument(offset) }
}

@Deprecated("设计过于傻逼,令人智熄", ReplaceWith("player(id)"))
fun <T> CommandContext<T>.playerAt(index: Int): ProxyPlayer {
    return onlinePlayers().first { it.name == get(index) }
}