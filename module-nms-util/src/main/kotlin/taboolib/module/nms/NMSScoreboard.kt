package taboolib.module.nms

import org.bukkit.entity.Player

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

    protected val universalTeamData: Class<*> by lazy {
        Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam\$b")
    }

    abstract fun setupScoreboard(player: Player, color: Boolean, title: String = "ScoreBoard")

    abstract fun setDisplayName(player: Player, title: String)

    abstract fun changeContent(player: Player, content: List<String>, lastContent: Map<Int, String>): Boolean

    abstract fun display(player: Player)
}