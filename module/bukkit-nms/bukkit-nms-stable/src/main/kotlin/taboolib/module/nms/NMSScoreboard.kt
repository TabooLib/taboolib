@file:Inject

package taboolib.module.nms

import net.minecraft.network.protocol.game.ClientboundResetScorePacket
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam
import net.minecraft.world.scores.DisplaySlot
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.tabooproject.reflex.Reflex.Companion.invokeConstructor
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import org.tabooproject.reflex.Reflex.Companion.unsafeInstance
import taboolib.common.Inject
import taboolib.common.platform.Ghost
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.type.PlayerScoreboard
import taboolib.module.nms.type.ChatColorFormat
import taboolib.platform.util.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 玩家记分板缓存
 */
private val playerScoreboardMap = ConcurrentHashMap<UUID, PlayerScoreboard>()

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
 * 进入游戏时移除记分板标记
 */
@Ghost
@SubscribeEvent(priority = EventPriority.LOWEST)
private fun onJoin(e: PlayerJoinEvent) {
    e.player.setMeta("t_scoreboard_objective_name", UUID.randomUUID().toString().substring(0..7))
    e.player.removeMeta("t_scoreboard_init")
}

/**
 * 离开游戏时释放记分板缓存
 */
@Ghost
@SubscribeEvent
private fun onQuit(e: PlayerQuitEvent) {
    // 移除记分板缓存
    playerScoreboardMap.remove(e.player.uniqueId)
}

/**
 * NMS 记分板操作接口
 */
abstract class NMSScoreboard {

    /**
     * 初始化记分板
     * @param player 玩家
     * @param color 是否启用颜色
     * @param title 记分板标题
     */
    abstract fun setupScoreboard(player: Player, color: Boolean, title: String = "ScoreBoard")

    /** 设置记分板标题 */
    abstract fun setDisplayName(player: Player, title: String)

    /**
     * 修改记分板内容
     * @param content 记分板内容
     * @param lastContent 上一次的记分板内容（用于比对是否需要更新）
     */
    abstract fun changeContent(player: Player, content: List<String>, lastContent: Map<Int, String>): Boolean

    /** 显示记分板 */
    abstract fun display(player: Player)

    /**
     * 更新玩家队伍
     * @param player 需要设置前缀或后缀的玩家
     * @param prefix 前缀
     * @param suffix 后缀
     * @param color 颜色
     * @param createTeam 是否需要创建队伍
     * @param target 向该玩家发包, 如果为空则为全体发包
     */
    abstract fun updateTeam(player: Player, prefix: String, suffix: String, color: ChatColorFormat, createTeam: Boolean, target: Player?)
}

// region NMSScoreboardImpl
@Suppress("unused", "DuplicatedCode")
class NMSScoreboardImpl : NMSScoreboard() {

    val uniqueOwner = listOf("§黒", "§黓", "§黔", "§黕", "§黖", "§黗", "§默", "§黙", "§黚", "§黛", "§黜", "§黝", "§點", "§黟", "§黠", "§黡", "§黢", "§黣", "§黤", "§黥", "§黦")

    val universalTeamData: Class<*> by unsafeLazy { PacketPlayOutScoreboardTeam::class.java.declaredClasses[0] }

    fun getObjectiveName(player: Player): String {
        return player.getMetaFirstOrNull("t_scoreboard_objective_name")?.asString() ?: player.uniqueId.toString().substring(0..7)
    }

    override fun setupScoreboard(player: Player, color: Boolean, title: String) {
        val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.setProperty("objectiveName", getObjectiveName(player))
            packet.setProperty("displayName", component(title))
            packet.setProperty("renderType", net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.setProperty("method", 0)
            // 1.21
            if (MinecraftVersion.versionId >= 12005) {
                packet.setProperty("numberFormat", Optional.empty<Any>())
            }
        } else {
            handle2DuplicatedPacket(player, packet, title)
            packet.setProperty("d", 0)
        }
        player.sendPacket(packet)
        // 初始化颜色
        if (color) initTeam(player)
    }

    /**
     *     public static final int METHOD_ADD = 0;
     *     public static final int METHOD_REMOVE = 1;
     *     public static final int METHOD_CHANGE = 2;
     */
    override fun changeContent(player: Player, content: List<String>, lastContent: Map<Int, String>): Boolean {
        val objectiveName = getObjectiveName(player)
        if (content.isEmpty()) {
            val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
            if (MinecraftVersion.isUniversal) {
                packet.setProperty("objectiveName", objectiveName)
                packet.setProperty("displayName", component("ScoreBoard"))
                packet.setProperty("renderType", net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
                packet.setProperty("method", 1)
                // 1.21
                if (MinecraftVersion.versionId >= 12005) {
                    packet.setProperty("numberFormat", Optional.empty<Any>())
                }
            }
            // region Legacy Version
            else {
                packet.setProperty("a", objectiveName)
                if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
                    packet.setProperty("b", component("ScoreBoard"))
                } else {
                    packet.setProperty("b", "ScoreBoard")
                }
                packet.setProperty("c", net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
                packet.setProperty("d", 1)
            }
            // endregion
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
        val objectiveName = getObjectiveName(player)
        val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardDisplayObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            // 1.20.2
            // Cannot cast java.lang.Integer to net.minecraft.world.scores.DisplaySlot
            packet.setProperty("slot", if (MinecraftVersion.versionId >= 12002) DisplaySlot.SIDEBAR else 1)
            packet.setProperty("objectiveName", objectiveName)
        }
        // region Legacy Version
        else {
            packet.setProperty("a", 1)
            packet.setProperty("b", objectiveName)
        }
        // endregion
        player.sendPacket(packet)
    }

    override fun setDisplayName(player: Player, title: String) {
        val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.setProperty("objectiveName", getObjectiveName(player))
            packet.setProperty("displayName", component(title))
            packet.setProperty("renderType", net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.setProperty("method", 2)
            // 1.21
            if (MinecraftVersion.versionId >= 12005) {
                packet.setProperty("numberFormat", Optional.empty<Any>())
            }
        }
        // region Legacy Version
        else {
            handle2DuplicatedPacket(player, packet, title)
            packet.setProperty("d", 2)
        }
        // endregion
        player.sendPacket(packet)
    }

    /**
     * player -> 需要设置前缀或后缀的玩家
     * target -> 向该玩家发包,如果为Null则为全体发包
     *
     *     private static final int METHOD_ADD = 0;
     *     private static final int METHOD_REMOVE = 1;
     *     private static final int METHOD_CHANGE = 2;
     *     private static final int METHOD_JOIN = 3;
     *     private static final int METHOD_LEAVE = 4;
     */
    override fun updateTeam(player: Player, prefix: String, suffix: String, color: ChatColorFormat, createTeam: Boolean, target: Player?) {
        if (createTeam) {
            createTeam(player)
        }
        if (MinecraftVersion.isUniversal) {
            val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
            packet.setProperty("method", 2)
            packet.setProperty("name", player.displayName)
            // 队伍参数
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
        // region Legacy Version
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
        } else {
            target.sendPacket(packet)
        }
        // endregion
    }

    private fun component(text: String): Any {
        return if (MinecraftVersion.major >= 11) {
            if (text.startsWith("{") && text.endsWith("}")) {
                net.minecraft.server.v1_16_R3.IChatBaseComponent.ChatSerializer::class.java.invokeMethod<Any>("fromJson", text, isStatic = true)!!
            } else {
                net.minecraft.server.v1_16_R3.IChatBaseComponent::class.java.invokeMethod<Any>("literal", text, isStatic = true)!!
            }
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
                // 队伍参数
                val b = universalTeamData.unsafeInstance()
                b.setProperty("displayName", component(color))
                // 1.19 "unexpected null component"
                if (MinecraftVersion.major >= 11) {
                    b.setProperty("playerPrefix", net.minecraft.network.chat.IChatBaseComponent.empty())
                    b.setProperty("playerSuffix", net.minecraft.network.chat.IChatBaseComponent.empty())
                }
                handle1DuplicatedPacket(b, packet, player, net.minecraft.server.v1_16_R3.EnumChatFormat.RESET)
                return@forEach
            }
            // region Legacy Version
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
            // endregion
        }
        player.setMeta("t_scoreboard_init", true)
    }

    private fun createTeam(player: Player) {
        if (MinecraftVersion.isUniversal) {
            val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
            packet.setProperty("method", 0)
            packet.setProperty("name", player.displayName)
            packet.setProperty("players", listOf(player.name))
            // 队伍参数
            val b = universalTeamData.unsafeInstance()
            b.setProperty("displayName", component(player.displayName))
            handle1DuplicatedPacketAll(b, packet, net.minecraft.server.v1_16_R3.EnumChatFormat.RESET)
            return
        }
        // region Legacy Version
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
        // endregion
    }

    private fun validateLineCount(line: Int): Int {
        if (uniqueOwner.size < line) error("Lines size are larger than supported.")
        return line
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
            // 队伍参数
            val b = universalTeamData.unsafeInstance()
            b.setProperty("displayName", component(team))
            b.setProperty("playerPrefix", component(content))
            // 1.19 "unexpected null component"
            if (MinecraftVersion.major >= 11) {
                b.setProperty("playerSuffix", net.minecraft.network.chat.IChatBaseComponent.empty())
            }
            handle1DuplicatedPacket(b, packet, player, net.minecraft.server.v1_16_R3.EnumChatFormat.RESET)
            return
        }
        // region Legacy Version
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
        // endregion
    }

    private fun updateLineCount(player: Player, line: Int, lastLineCount: Int) {
        val objectiveName = getObjectiveName(player)
        // 行数变多了，新增行
        if (validateLineCount(line) > lastLineCount) {
            (lastLineCount until line).forEach { i ->
                // 1.20.5 后两个参数改为 Optional
                // String owner, String objectiveName, int score, Optional<IChatBaseComponent> display, Optional<NumberFormat> numberFormat
                if (MinecraftVersion.versionId >= 12005) {
                    player.sendPacket(PacketPlayOutScoreboardScore::class.java.invokeConstructor(uniqueOwner[i], objectiveName, i, Optional.empty<Any>(), Optional.empty<Any>()))
                    return@forEach
                }
                // region Legacy Version
                // 1.20.4 改为 Record
                // String owner, String objectiveName, int score, @Nullable IChatBaseComponent display, @Nullable NumberFormat numberFormat
                if (MinecraftVersion.majorLegacy > 12002) {
                    player.sendPacket(PacketPlayOutScoreboardScore::class.java.invokeConstructor(uniqueOwner[i], objectiveName, i, null, null))
                    return@forEach
                }
                // 1.13+ 直接实例化
                if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
                    player.sendPacket(
                        net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore(
                            net.minecraft.server.v1_16_R3.ScoreboardServer.Action.CHANGE,
                            objectiveName,
                            uniqueOwner[i],
                            i
                        )
                    )
                    return@forEach
                }
                // 1.12 反射处理
                val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore()
                packet.setProperty("a", uniqueOwner[i])
                packet.setProperty("b", objectiveName)
                packet.setProperty("c", i)
                packet.setProperty("d", net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE)
                player.sendPacket(packet)
                // endregion
            }
        }
        // 变少了，减少行
        else {
            (line until lastLineCount).forEach { i ->
                // 1.20.4
                // 变成单独一个包了 -> ClientboundResetScorePacket
                if (MinecraftVersion.majorLegacy > 12002) {
                    player.sendPacket(ClientboundResetScorePacket::class.java.invokeConstructor(uniqueOwner[i], objectiveName))
                    return@forEach
                }
                // region Legacy Version
                // 1.13+
                if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
                    player.sendPacket(
                        net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore(
                            net.minecraft.server.v1_16_R3.ScoreboardServer.Action.REMOVE,
                            uniqueOwner[i],
                            objectiveName,
                            i
                        )
                    )
                    return@forEach
                }
                val packet = net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore()
                packet.setProperty("a", uniqueOwner[i])
                packet.setProperty("b", objectiveName)
                packet.setProperty("d", net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore.EnumScoreboardAction.REMOVE)
                player.sendPacket(packet)
                // endregion
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
        Bukkit.getServer().onlinePlayers.forEach { it.sendPacket(packet) }
    }

    private fun handle2DuplicatedPacket(player: Player, packet: Any, title: String) {
        packet.setProperty("a", getObjectiveName(player))
        if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
            packet.setProperty("b", component(title))
        } else {
            packet.setProperty("b", title)
        }
        packet.setProperty("c", net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
    }
}
// endregion