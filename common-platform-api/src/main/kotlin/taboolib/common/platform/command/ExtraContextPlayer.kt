package taboolib.common.platform.command

import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.onlinePlayers

/**
 * 根据节点名称获取输入参数并转换为玩家
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者玩家不存在
 */
fun <T> CommandContext<T>.player(id: String): ProxyPlayer {
    return getProxyPlayer(get(id).substringBefore(' '))!!
}

/**
 * 根据节点名称获取输入参数并转换为玩家
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 */
fun <T> CommandContext<T>.playerOrNull(id: String): ProxyPlayer? {
    return getProxyPlayer(getOrNull(id)?.substringBefore(' ') ?: return null)
}

/**
 * 根据节点名称获取输入参数并转换为玩家
 * 根据输入的参数，如果是 "*" 则获取所有在线玩家，反之与 [player] 一致
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者玩家不存在
 */
fun <T> CommandContext<T>.players(id: String): List<ProxyPlayer> {
    val text = get(id).substringBefore(' ')
    return if (text == "*") onlinePlayers() else listOf(getProxyPlayer(text)!!)
}

/**
 * 根据节点名称获取输入参数并转换为玩家
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 */
fun <T> CommandContext<T>.playersOrNull(id: String): List<ProxyPlayer>? {
    val text = getOrNull(id)?.substringBefore(' ') ?: return null
    return if (text == "*") onlinePlayers() else listOf(getProxyPlayer(text) ?: return null)
}