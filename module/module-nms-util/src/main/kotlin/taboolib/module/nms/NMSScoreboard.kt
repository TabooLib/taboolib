package taboolib.module.nms

import net.minecraft.world.scores.DisplaySlot
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import org.tabooproject.reflex.Reflex.Companion.unsafeInstance
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.type.ChatColorFormat
import taboolib.module.nms.type.PlayerScoreboard
import taboolib.platform.BukkitPlugin
import taboolib.platform.util.hasMeta
import taboolib.platform.util.onlinePlayers
import taboolib.platform.util.removeMeta
import taboolib.platform.util.setMeta
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 发送记分板数据包
 * @param content 记分板内容（设置为空时注销记分板）
 */
fun Player.sendScoreboard(vararg content: String) {
    val scoreboard = playerScoreboardMap.getOrPut(uniqueId) { PlayerScoreboard(this) }
    if (content.isEmpty()) {
        scoreboard.sendContent(emptyList())
    } else {
        scoreboard.sendTitle(content.firstOrNull().toString())
        scoreboard.sendContent(content.filterIndexed { index, _ -> index > 0 })
    }
}

/**
 * 发送记分板数据包
 * @param prefix 前缀,传入""时为清除前缀
 * @param player 发包给的玩家,传入Null时为给全体发送
 */
fun Player.setPrefix(prefix: String, player: Player?) {
    val scoreboard = playerScoreboardMap.getOrPut(uniqueId) { PlayerScoreboard(this) }
    if (prefix.isNotEmpty()) {
        scoreboard.setPrefix(prefix, player)
    } else {
        scoreboard.clearPrefix(player)
    }
}

/**
 * 修改后缀
 * @param suffix 后缀,传入""时为清除后缀
 *  * @param player 发包给的玩家,传入Null时为给全体发送
 */
fun Player.setSuffix(suffix: String, player: Player?) {
    val scoreboard = playerScoreboardMap.getOrPut(uniqueId) { PlayerScoreboard(this) }
    if (suffix.isNotEmpty()) {
        scoreboard.setSuffix(suffix, player)
    } else {
        scoreboard.clearSuffix(player)
    }
}

/**
 * 修改颜色
 * @param color 颜色
 * @param target 数据包接收单位, 传入 null 时为给全体发送
 */
fun Player.setTeamColor(color: ChatColorFormat, target: Player? = null) {
    playerScoreboardMap.getOrPut(uniqueId) { PlayerScoreboard(this) }.setColor(color, target)
}

/**
 * 玩家记分板缓存
 */
private val playerScoreboardMap = ConcurrentHashMap<UUID, PlayerScoreboard>()

/**
 * 进入游戏时移除记分板标记
 */
@SubscribeEvent(priority = EventPriority.LOWEST)
private fun onJoin(e: PlayerJoinEvent) {
    e.player.removeMeta("t_scoreboard_init")
}

/**
 * 离开游戏时释放记分板缓存
 */
@SubscribeEvent
private fun onQuit(e: PlayerQuitEvent) {
    // 移除记分板缓存
    playerScoreboardMap.remove(e.player.uniqueId)
}

/**
 * NMS 记分板操作接口
 */
abstract class NMSScoreboard {

    abstract fun setupScoreboard(player: Player, color: Boolean, title: String = "ScoreBoard")

    abstract fun setDisplayName(player: Player, title: String)

    abstract fun changeContent(player: Player, content: List<String>, lastContent: Map<Int, String>): Boolean

    abstract fun display(player: Player)

    abstract fun updateTeam(player: Player, prefix: String, suffix: String, color: ChatColorFormat, created: Boolean, target: Player?)
}

@Suppress("unused", "DuplicatedCode", "SpellCheckingInspection")
class NMSScoreboardImpl : NMSScoreboard() {

    val uniqueOwner = listOf("§黒", "§黓", "§黔", "§黕", "§黖", "§黗", "§默", "§黙", "§黚", "§黛", "§黜", "§黝", "§點", "§黟", "§黠", "§黡", "§黢", "§黣", "§黤", "§黥", "§黦")

    val objectiveName = UUID.randomUUID().toString().substring(0..7)

    val universalTeamData: Class<*> by unsafeLazy {
        Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam\$b")
    }

    override fun setupScoreboard(player: Player, color: Boolean, title: String) {
        val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.setProperty("objectiveName", objectiveName)
            packet.setProperty("displayName", component(title))
            packet.setProperty("renderType", net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.setProperty("method", 0)
        } else {
            handle2DuplicatedPacket(packet, title)
            packet.setProperty("d", 0)
        }
        player.sendPacket(packet)
        if (color) {
            initTeam(player)
        }
    }

    override fun changeContent(player: Player, content: List<String>, lastContent: Map<Int, String>): Boolean {
        if (content.isEmpty()) {
            val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
            if (MinecraftVersion.isUniversal) {
                packet.setProperty("objectiveName", objectiveName)
                packet.setProperty("displayName", component("ScoreBoard"))
                packet.setProperty("renderType", net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
                packet.setProperty("method", 1)
            } else {
                packet.setProperty("a", objectiveName)
                if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
                    packet.setProperty("b", component("ScoreBoard"))
                } else {
                    packet.setProperty("b", "ScoreBoard")
                }
                packet.setProperty("c", net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
                packet.setProperty("d", 1)
            }
            player.sendPacket(packet)
            return true
        }
        val update = content.size != lastContent.size
        if (update) {
            updateLineCount(player, content.size, lastContent.size)
        }
        content.forEachIndexed { line, ct ->
            if (update || ct != lastContent[line]) {
                sendTeamPrefixSuffix(player, uniqueOwner[content.size - line - 1], ct)
            }
        }
        return false
    }

    override fun display(player: Player) {
        val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardDisplayObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            // 1.12.2
            // Cannot cast java.lang.Integer to net.minecraft.world.scores.DisplaySlot
            if (MinecraftVersion.majorLegacy >= 12002) {
                packet.setProperty("slot", DisplaySlot.SIDEBAR)
            } else {
                packet.setProperty("slot", 1)
            }
            packet.setProperty("objectiveName", objectiveName)
        } else {
            packet.setProperty("a", 1)
            packet.setProperty("b", objectiveName)
        }
        player.sendPacket(packet)
    }

    override fun setDisplayName(player: Player, title: String) {
        val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.setProperty("objectiveName", objectiveName)
            packet.setProperty("displayName", component(title))
            packet.setProperty("renderType", net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.setProperty("method", 2)
        } else {
            handle2DuplicatedPacket(packet, title)
            packet.setProperty("d", 2)
        }
        player.sendPacket(packet)
    }

    /**
     * player -> 需要设置前缀或后缀的玩家
     * p -> 向该玩家发包,如果为Null则为全体发包
     *
     * @see EnumChatFormat
     * @see PacketPlayOutScoreboardTeam
     */
    override fun updateTeam(player: Player, prefix: String, suffix: String, color: ChatColorFormat, created: Boolean, target: Player?) {
        if (created) {
            createTeam(player)
        }
        if (MinecraftVersion.isUniversal) {
            val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
            packet.setProperty("method", 2)
            packet.setProperty("name", player.displayName)
            val b = universalTeamData.unsafeInstance()
            b.setProperty("displayName", component(player.displayName))
            b.setProperty("playerPrefix", component(prefix))
            b.setProperty("playerSuffix", component(suffix))
            if (target == null) {
                handle1DuplicatedPacketAll(b, packet, net.minecraft.server.v1_16_R3.EnumChatFormat.valueOf(color.name.uppercase()))
            } else {
                handle1DuplicatedPacket(b, packet, target, net.minecraft.server.v1_16_R3.EnumChatFormat.valueOf(color.name.uppercase()))
            }
            return
        }
        if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
            val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam()
            packet.setProperty("a", player.displayName)
            packet.setProperty("c", component(prefix))
            packet.setProperty("d", component(suffix))
            packet.setProperty("i", 2)
            if (target == null) {
                onlinePlayers.forEach { pp -> pp.sendPacket(packet) }
            } else target.sendPacket(packet)
            return
        }
        val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam()
        packet.setProperty("a", player.displayName)
        packet.setProperty("c", prefix)
        packet.setProperty("d", suffix)
        if (target == null) {
            onlinePlayers.forEach { pp -> pp.sendPacket(packet) }
        } else target.sendPacket(packet)
    }

    private fun component(text: String): Any {
        return if (MinecraftVersion.major >= 11) {
            net.minecraft.server.v1_16_R3.IChatBaseComponent::class.java.invokeMethod<Any>("literal", text, isStatic = true)!!
        } else {
            net.minecraft.server.v1_16_R3.ChatComponentText(text)
        }
    }

    /**
     * a -> Team Name
     * b -> Team Display Name
     * c -> Team Prefix
     * d -> Team Suffix
     * e -> Name Tag Visibility
     * f -> Color
     * g -> Players, Player Count
     * h -> Mode
     *
     *  If 0 then the team is created.
     *  If 1 then the team is removed.
     *  If 2 the team information is updated.
     *  If 3 then new players are added to the team.
     *  If 4 then players are removed from the team.
     *
     * i -> Friendly Fire
     *
     * @see EnumChatFormat
     * @see PacketPlayOutScoreboardTeam
     */
    private fun initTeam(player: Player) {
        if (player.hasMeta("t_scoreboard_init")) {
            return
        }
        uniqueOwner.forEach { color ->
            if (MinecraftVersion.isUniversal) {
                val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
                packet.setProperty("method", 0)
                packet.setProperty("name", color)
                packet.setProperty("players", listOf(color))
                val b = universalTeamData.unsafeInstance()
                b.setProperty("displayName", component(color))
                if (MinecraftVersion.major >= 11) { // 1.19 "unexpected null component"
                    b.setProperty("playerPrefix", net.minecraft.network.chat.IChatBaseComponent.empty())
                    b.setProperty("playerSuffix", net.minecraft.network.chat.IChatBaseComponent.empty())
                }
                handle1DuplicatedPacket(b, packet, player, net.minecraft.server.v1_16_R3.EnumChatFormat.RESET)
                return@forEach
            }
            if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
                val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam()
                packet.setProperty("a", color)
                packet.setProperty("b", component(color))
                packet.setProperty("e", "always")
                packet.setProperty("f", "always")
                packet.setProperty("g", net.minecraft.server.v1_16_R3.EnumChatFormat.RESET)
                packet.setProperty("h", listOf(color))
                packet.setProperty("i", 0)
                packet.setProperty("j", -1)
                player.sendPacket(packet)
                return@forEach
            }
            val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam()
            packet.setProperty("a", color)
            packet.setProperty("b", color)
            packet.setProperty("e", net.minecraft.server.v1_16_R3.ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS.e)
            // Collections$SingletonList cannot be cast to java.lang.Number
            if (MinecraftVersion.major >= 1) {
                packet.setProperty("f", "always")
                packet.setProperty("g", -1)
                packet.setProperty("h", listOf(color))
                packet.setProperty("i", 0)
            } else {
                packet.setProperty("f", -1)
                packet.setProperty("g", listOf(color))
                packet.setProperty("h", 0)
            }
            player.sendPacket(packet)
        }
        player.setMeta("t_scoreboard_init", true)
    }

    private fun createTeam(player: Player) {
        if (MinecraftVersion.isUniversal) {
            val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
            packet.setProperty("method", 0)
            packet.setProperty("name", player.displayName)
            packet.setProperty("players", listOf(player.name))
            val b = universalTeamData.unsafeInstance()
            b.setProperty("displayName", component(player.displayName))
            handle1DuplicatedPacketAll(b, packet, net.minecraft.server.v1_16_R3.EnumChatFormat.RESET)
            return
        }
        if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
            val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam()
            packet.setProperty("a", player.displayName)
            packet.setProperty("b", component(player.displayName))
            packet.setProperty("e", "always")
            packet.setProperty("f", "always")
            packet.setProperty("g", net.minecraft.server.v1_16_R3.EnumChatFormat.RESET)
            packet.setProperty("h", listOf(player.displayName))
            packet.setProperty("i", 0)
            packet.setProperty("j", -1)
            onlinePlayers.forEach { p -> p.sendPacket(packet) }
            return
        }
        val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam()
        packet.setProperty("a", player.displayName)
        packet.setProperty("b", player.displayName)
        packet.setProperty("e", net.minecraft.server.v1_16_R3.ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS.e)
        if (MinecraftVersion.major >= 1) {
            packet.setProperty("f", "always")
            packet.setProperty("g", -1)
            packet.setProperty("h", listOf(player.displayName))
            packet.setProperty("i", 0)
        } else {
            packet.setProperty("f", -1)
            packet.setProperty("g", listOf(player.displayName))
            packet.setProperty("h", 0)
        }
        onlinePlayers.forEach { p -> p.sendPacket(packet) }
    }

    private fun validateLineCount(line: Int) {
        if (uniqueOwner.size < line) {
            throw IllegalArgumentException("Lines size are larger than supported.")
        }
    }

    /**
     * @param team 为\[content.size - line - 1\]
     */
    private fun sendTeamPrefixSuffix(player: Player, team: String, content: String) {
        if (MinecraftVersion.major >= 9) {
            val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
            packet.setProperty("method", 2)
            packet.setProperty("name", team)
            packet.setProperty("players", listOf(team))
            val b = universalTeamData.unsafeInstance()
            b.setProperty("displayName", component(team))
            b.setProperty("playerPrefix", component(content))
            if (MinecraftVersion.major >= 11) { // 1.19 "unexpected null component"
                b.setProperty("playerSuffix", net.minecraft.network.chat.IChatBaseComponent.empty())
            }
            handle1DuplicatedPacket(b, packet, player, net.minecraft.server.v1_16_R3.EnumChatFormat.RESET)
            return
        }
        if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
            val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam()
            packet.setProperty("a", team) // 1.17 -> name
            packet.setProperty("c", component(content)) // 1.17 -> playerPrefix
            packet.setProperty("i", 2) // 1.17 -> method
            player.sendPacket(packet)
            return
        }
        var prefix = content
        var suffix = ""
        if (content.length > 16) {
            prefix = content.substring(0 until 16)
            if (prefix.endsWith("§")) {
                prefix = prefix.removeSuffix("§")
                suffix = "§" + content.substring(16 until content.length)
            } else {
                val color = ChatColor.getLastColors(prefix)
                suffix = color + content.substring(16 until content.length)
            }
            if (suffix.length > 16) {
                suffix = suffix.substring(0, 16)
            }
        }
        val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam()
        packet.setProperty("a", team)
        packet.setProperty(if (MinecraftVersion.major >= 1) "i" else "h", 2)
        packet.setProperty("c", prefix)
        packet.setProperty("d", suffix)
        player.sendPacket(packet)
    }

    private fun updateLineCount(player: Player, line: Int, lastLineCount: Int) {
        validateLineCount(line)
        if (line > lastLineCount) {
            (lastLineCount until line).forEach { i ->
                if (MinecraftVersion.major >= 9) {
                    val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore::class.java.unsafeInstance()
                    packet.setProperty("owner", uniqueOwner[i])
                    packet.setProperty("objectiveName", objectiveName)
                    packet.setProperty("score", i)
                    packet.setProperty("method", net.minecraft.server.v1_16_R3.ScoreboardServer.Action.CHANGE)
                    player.sendPacket(packet)
                    return@forEach
                }
                if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
                    val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore()
                    packet.setProperty("a", uniqueOwner[i])
                    packet.setProperty("b", objectiveName)
                    packet.setProperty("c", i)
                    packet.setProperty("d", net.minecraft.server.v1_16_R3.ScoreboardServer.Action.CHANGE)
                    player.sendPacket(packet)
                    return@forEach
                }
                val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore()
                packet.setProperty("a", uniqueOwner[i])
                packet.setProperty("b", objectiveName)
                packet.setProperty("c", i)
                packet.setProperty(
                    "d", net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE
                )
                player.sendPacket(packet)
            }
        } else {
            (line until lastLineCount).forEach { i ->
                if (MinecraftVersion.major >= 9) {
                    val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore::class.java.unsafeInstance()
                    packet.setProperty("owner", uniqueOwner[i])
                    packet.setProperty("objectiveName", objectiveName)
                    packet.setProperty("method", net.minecraft.server.v1_16_R3.ScoreboardServer.Action.REMOVE)
                    player.sendPacket(packet)
                    return@forEach
                }
                if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
                    val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore()
                    packet.setProperty("a", uniqueOwner[i])
                    packet.setProperty("b", objectiveName)
                    packet.setProperty("d", net.minecraft.server.v1_16_R3.ScoreboardServer.Action.REMOVE)
                    player.sendPacket(packet)
                    return@forEach
                }
                val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore()
                packet.setProperty("a", uniqueOwner[i])
                packet.setProperty("b", objectiveName)
                packet.setProperty(
                    "d", net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore.EnumScoreboardAction.REMOVE
                )
                player.sendPacket(packet)
            }
        }
    }

    private fun handle1DuplicatedPacket(b: Any, packet: Any, player: Player, color: net.minecraft.server.v1_16_R3.EnumChatFormat) {
        b.setProperty("nametagVisibility", "always")
        b.setProperty("collisionRule", "always")
        b.setProperty("color", color)
        b.setProperty("options", 3)
        packet.setProperty("parameters", Optional.of(b))
        player.sendPacket(packet)
    }

    private fun handle1DuplicatedPacketAll(b: Any, packet: Any, color: net.minecraft.server.v1_16_R3.EnumChatFormat) {
        b.setProperty("nametagVisibility", "always")
        b.setProperty("collisionRule", "always")
        b.setProperty("color", color)
        b.setProperty("options", 3)
        packet.setProperty("parameters", Optional.of(b))
        for (p in BukkitPlugin.getInstance().server.onlinePlayers) {
            p.sendPacket(packet)
        }
    }

    private fun handle2DuplicatedPacket(packet: Any, title: String) {
        packet.setProperty("a", objectiveName)
        if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
            packet.setProperty("b", component(title))
        } else {
            packet.setProperty("b", title)
        }
        packet.setProperty("c", net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
    }
}