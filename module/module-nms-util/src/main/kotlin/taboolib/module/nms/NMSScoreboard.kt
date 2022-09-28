package taboolib.module.nms

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.TabooLibCommon
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.type.ChatColorFormat
import taboolib.module.nms.type.PlayerScoreboard
import taboolib.platform.util.removeMeta
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal val nmsScoreboard by unsafeLazy { nmsProxy<NMSScoreboard>() }

internal val playerScoreboardMap = ConcurrentHashMap<UUID, PlayerScoreboard>().also {
    // 卸载时不再注册事件
    if (TabooLibCommon.getLifeCycle() == LifeCycle.DISABLE) {
        return@also
    }
    registerBukkitListener(PlayerQuitEvent::class.java, priority = EventPriority.NORMAL) { event ->
        it.remove(event.player.uniqueId)
        event.player.removeMeta("t_scoreboard_init")
    }
}

/**
 * 发送记分板数据包
 * @param content 记分板内容（设置为空时注销记分板）
 */
fun Player.sendScoreboard(vararg content: String) {
    val scoreboardObj = playerScoreboardMap.computeIfAbsent(uniqueId) {
        return@computeIfAbsent PlayerScoreboard(this)
    }
    if (content.isEmpty()) {
        scoreboardObj.sendContent(emptyList())
        return
    }
    scoreboardObj.sendTitle(content.firstOrNull().toString())
    scoreboardObj.sendContent(content.filterIndexed { index, _ -> index > 0 })
}

/**
 * 发送记分板数据包
 * @param prefix 前缀,传入""时为清除前缀
 * @param player 发包给的玩家,传入Null时为给全体发送
 */
fun Player.setPrefix(prefix: String, player: Player?) {
    val scoreboardObj = playerScoreboardMap.computeIfAbsent(uniqueId) {
        return@computeIfAbsent PlayerScoreboard(this)
    }
    if (prefix == "") {
        scoreboardObj.clearPrefix(player)
        return
    }
    scoreboardObj.setPrefix(prefix, player)
}

/**
 * 修改后缀
 * @param suffix 后缀,传入""时为清除后缀
 *  * @param player 发包给的玩家,传入Null时为给全体发送
 */
fun Player.setSuffix(suffix: String, player: Player?) {
    val scoreboardObj = playerScoreboardMap.computeIfAbsent(uniqueId) {
        return@computeIfAbsent PlayerScoreboard(this)
    }
    if (suffix == "") {
        scoreboardObj.clearSuffix(player)
        return
    }
    scoreboardObj.setSuffix(suffix, player)
}

/**
 * 修改颜色
 * @param color 颜色
 * @param target 数据包接收单位, 传入 null 时为给全体发送
 */
fun Player.setTeamColor(color: ChatColorFormat, target: Player? = null) {
    val scoreboardObj = playerScoreboardMap.computeIfAbsent(uniqueId) {
        return@computeIfAbsent PlayerScoreboard(this)
    }
    scoreboardObj.setColor(color, target)
}

/**
 * NMS 记分板操作接口
 */
abstract class NMSScoreboard {

    val uniqueColors = listOf(
        "§黒",
        "§黓",
        "§黔",
        "§黕",
        "§黖",
        "§黗",
        "§默",
        "§黙",
        "§黚",
        "§黛",
        "§黜",
        "§黝",
        "§點",
        "§黟",
        "§黠",
        "§黡",
        "§黢",
        "§黣",
        "§黤",
        "§黥",
        "§黦",
        "§黧",
        "§黨",
        "§黩",
        "§黪",
        "§黫",
        "§黬",
        "§黭",
    )
    val key = UUID.randomUUID().toString().substring(0..7)

    protected val universalTeamData: Class<*> by lazy {
        Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam\$b")
    }

    abstract fun setupScoreboard(player: Player, color: Boolean, title: String = "ScoreBoard")

    abstract fun setDisplayName(player: Player, title: String)

    abstract fun changeContent(player: Player, content: List<String>, lastContent: Map<Int, String>): Boolean

    abstract fun display(player: Player)

    abstract fun updateTeam(
        player: Player,
        prefix: String,
        suffix: String,
        color: ChatColorFormat,
        created: Boolean,
        target: Player?
    )
}