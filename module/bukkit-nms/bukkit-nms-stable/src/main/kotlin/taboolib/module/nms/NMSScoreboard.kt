@file:Inject

package taboolib.module.nms

import net.minecraft.EnumChatFormat
import net.minecraft.core.IRegistryCustom
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.chat.numbers.BlankFormat
import net.minecraft.network.protocol.game.*
import net.minecraft.server.v1_12_R1.ScoreboardScore
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.ScoreboardObjective
import net.minecraft.world.scores.ScoreboardTeam
import net.minecraft.world.scores.criteria.IScoreboardCriteria
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_21_R3.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.tabooproject.reflex.Reflex.Companion.invokeConstructor
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common.Inject
import taboolib.common.platform.Ghost
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.t
import taboolib.module.nms.type.ChatColorFormat
import taboolib.module.nms.type.PlayerScoreboard
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
    abstract fun updateTeam(
        player: Player,
        prefix: String,
        suffix: String,
        color: ChatColorFormat,
        createTeam: Boolean,
        target: Player?
    )
}

// region NMSScoreboardImpl
@Suppress("unused", "DuplicatedCode")
class NMSScoreboardImpl : NMSScoreboard() {

    val uniqueOwner = listOf(
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
        "§黦"
    )

    val version = MinecraftVersion.versionId

    fun getObjectiveName(player: Player): String {
        return player.getMetaFirstOrNull("t_scoreboard_objective_name")?.asString() ?: player.uniqueId.toString()
            .substring(0..7)
    }

    override fun setupScoreboard(player: Player, color: Boolean, title: String) {
        val objectiveName = getObjectiveName(player)
        val score = if (MinecraftVersion.isUniversal) {
            if (version >= 12003) {
                ScoreboardObjective(
                    Scoreboard(),
                    objectiveName,
                    IScoreboardCriteria.AIR,
                    component(title) as IChatBaseComponent,
                    IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER,
                    true,
                    BlankFormat.INSTANCE
                )
            } else {
                // 1.20.1 及更早版本使用反射调用 5 参构造
                ScoreboardObjective::class.java.invokeConstructor(
                    Scoreboard(),
                    objectiveName,
                    IScoreboardCriteria.AIR,
                    component(title),
                    IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER
                )
            }
        } else {
            if (version >= 11300) {
                ScoreboardObjective::class.java.invokeConstructor(
                    net.minecraft.server.v1_16_R3.Scoreboard(),
                    objectiveName,
                    net.minecraft.server.v1_16_R3.IScoreboardCriteria.AIR,
                    component(title),
                    net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER
                )
            } else {
                ScoreboardObjective::class.java.invokeConstructor(
                    net.minecraft.server.v1_12_R1.Scoreboard(),
                    objectiveName,
                    net.minecraft.server.v1_12_R1.IScoreboardCriteria.i
                ).apply { setProperty("e", title) }
            }
        }
        player.sendPacket(PacketPlayOutScoreboardObjective(score, 0))
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
            val score = if (MinecraftVersion.isUniversal) {
                if (version >= 12003) {
                    ScoreboardObjective(
                        Scoreboard(),
                        objectiveName,
                        IScoreboardCriteria.AIR,
                        component("ScoreBoard") as IChatBaseComponent,
                        IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER,
                        true,
                        BlankFormat.INSTANCE
                    )
                } else {
                    // 1.20.1 及更早使用反射调用 5 参构造
                    ScoreboardObjective::class.java.invokeConstructor(
                        Scoreboard(),
                        objectiveName,
                        IScoreboardCriteria.AIR,
                        component("ScoreBoard") as IChatBaseComponent,
                        IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER
                    )
                }
            }
            // region Legacy Version
            else {
                if (version >= 11300) {
                    ScoreboardObjective::class.java.invokeConstructor(
                        net.minecraft.server.v1_16_R3.Scoreboard(),
                        objectiveName,
                        net.minecraft.server.v1_16_R3.IScoreboardCriteria.AIR,
                        component("ScoreBoard"),
                        net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER
                    )
                } else {
                    ScoreboardObjective::class.java.invokeConstructor(
                        net.minecraft.server.v1_12_R1.Scoreboard(),
                        objectiveName,
                        net.minecraft.server.v1_12_R1.IScoreboardCriteria.i
                    ).apply { setProperty("e", "ScoreBoard") }
                }
            }
            // endregion
            player.sendPacket(PacketPlayOutScoreboardObjective(score, 1))
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
        val packet = if (MinecraftVersion.isUniversal) {
            if (version >= 12003) {
                PacketPlayOutScoreboardDisplayObjective(
                    DisplaySlot.SIDEBAR, ScoreboardObjective(
                        Scoreboard(),
                        objectiveName,
                        IScoreboardCriteria.AIR,
                        IChatBaseComponent.empty(),
                        IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER,
                        true,
                        BlankFormat.INSTANCE
                    )
                )
            } else {
                PacketPlayOutScoreboardDisplayObjective::class.java.invokeConstructor(
                    1, ScoreboardObjective::class.java.invokeConstructor(
                        Scoreboard(),
                        objectiveName,
                        IScoreboardCriteria.AIR,
                        IChatBaseComponent.empty(),
                        IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER
                    )
                )
            }
        }
        // region Legacy Version
        else {
            if (version >= 11300) {
                PacketPlayOutScoreboardDisplayObjective::class.java.invokeConstructor(
                    1, net.minecraft.server.v1_16_R3.ScoreboardObjective(
                        net.minecraft.server.v1_16_R3.Scoreboard(),
                        objectiveName,
                        net.minecraft.server.v1_16_R3.IScoreboardCriteria.AIR,
                        net.minecraft.server.v1_16_R3.ChatComponentText(""),
                        net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER
                    )
                )
            } else {
                PacketPlayOutScoreboardDisplayObjective::class.java.invokeConstructor(
                    1, ScoreboardObjective::class.java.invokeConstructor(
                        net.minecraft.server.v1_12_R1.Scoreboard(),
                        objectiveName,
                        net.minecraft.server.v1_12_R1.IScoreboardCriteria.i
                    )
                )
            }
        }
        // endregion
        player.sendPacket(packet)
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun setDisplayName(player: Player, title: String) {
        val objectiveName = getObjectiveName(player)
        val score = if (MinecraftVersion.isUniversal) {
            if (version >= 12003) {
                ScoreboardObjective(
                    Scoreboard(),
                    objectiveName,
                    IScoreboardCriteria.AIR,
                    component(title) as IChatBaseComponent,
                    IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER,
                    true,
                    BlankFormat.INSTANCE
                )
            } else {
                // 1.20.1 及更早使用反射调用 5 参构造
                ScoreboardObjective::class.java.invokeConstructor(
                    Scoreboard(),
                    objectiveName,
                    IScoreboardCriteria.AIR,
                    component(title) as IChatBaseComponent,
                    IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER
                )
            }
        }
        // region Legacy Version
        else {
            if (version >= 11300) {
                ScoreboardObjective::class.java.invokeConstructor(
                    net.minecraft.server.v1_16_R3.Scoreboard(),
                    objectiveName,
                    net.minecraft.server.v1_16_R3.IScoreboardCriteria.AIR,
                    component(title),
                    net.minecraft.server.v1_16_R3.IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER
                )
            } else {
                net.minecraft.server.v1_12_R1.ScoreboardObjective::class.java.invokeConstructor(
                    Scoreboard(), objectiveName, net.minecraft.server.v1_12_R1.IScoreboardCriteria.i
                ).also { it.setDisplayName(title) } as ScoreboardObjective
            }
        }
        // endregion
        player.sendPacket(PacketPlayOutScoreboardObjective(score, 2))
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
    override fun updateTeam(
        player: Player,
        prefix: String,
        suffix: String,
        color: ChatColorFormat,
        createTeam: Boolean,
        target: Player?
    ) {
        if (createTeam) {
            createTeam(player)
        }
        if (MinecraftVersion.isUniversal) {
            val team = ScoreboardTeam(Scoreboard(), player.displayName)
            // 队伍参数
            team.playerPrefix = component(prefix) as IChatBaseComponent
            team.playerSuffix = component(suffix) as IChatBaseComponent
            val packet = PacketPlayOutScoreboardTeam::class.java.invokeConstructor(
                player.displayName, 2, Optional.of(PacketPlayOutScoreboardTeam.b(team)), listOf<String>()
            )
            if (target == null) {
                Bukkit.getServer().onlinePlayers.forEach { it.sendPacket(packet) }
            } else {
                player.sendPacket(packet)
            }
            return
        }
        // region Legacy Version
        val team =
            net.minecraft.server.v1_12_R1.ScoreboardTeam(net.minecraft.server.v1_12_R1.Scoreboard(), player.displayName)
        team.prefix = prefix
        team.suffix = suffix
        val packet = net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam(team, 2)
        if (target == null) {
            onlinePlayers.forEach { pp -> pp.sendPacket(packet) }
        } else target.sendPacket(packet)
        // endregion
    }

    private fun component(text: String): Any {
        return if (text.startsWith("{") && text.endsWith("}")) {
            listOf(
                {
                    net.minecraft.server.v1_16_R3.IChatBaseComponent.ChatSerializer::class.java.invokeMethod<Any>(
                        "fromJson",
                        text,
                        isStatic = true
                    )!!
                },
                { net.minecraft.server.v1_16_R3.IChatBaseComponent.ChatSerializer.b(text)!! },
                { IChatBaseComponent.ChatSerializer.fromJson(text, IRegistryCustom.EMPTY)!! },
                { CraftChatMessage.fromJSON(text) }
            ).firstNotNullOf { runCatching(it).getOrNull() }
        } else {
            net.minecraft.server.v1_16_R3.IChatBaseComponent::class.java.invokeMethod<Any>(
                "literal",
                text,
                isStatic = true
            )!!
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
                // 队伍参数
                val team = ScoreboardTeam(Scoreboard(), color)
                player.sendPacket(
                    PacketPlayOutScoreboardTeam::class.java.invokeConstructor(
                        color, 0, Optional.of(PacketPlayOutScoreboardTeam.b(team)), listOf(color)
                    )
                )
                return@forEach
            }
            // region Legacy Version
            val team = net.minecraft.server.v1_12_R1.ScoreboardTeam(net.minecraft.server.v1_12_R1.Scoreboard(), color)
            val packet = net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam(team, 0)
            packet.setProperty("h", listOf(color))
            // endregion
            player.sendPacket(packet)
        }
        player.setMeta("t_scoreboard_init", true)
    }

    private fun createTeam(player: Player) {
        if (MinecraftVersion.isUniversal) {
            // 队伍参数
            val packet = PacketPlayOutScoreboardTeam::class.java.invokeConstructor(
                player.displayName,
                0,
                Optional.of(PacketPlayOutScoreboardTeam.b(ScoreboardTeam(Scoreboard(), player.displayName))),
                listOf(player.name)
            )
            Bukkit.getServer().onlinePlayers.forEach { it.sendPacket(packet) }
            return
        }
        // region Legacy Version
        val team =
            net.minecraft.server.v1_12_R1.ScoreboardTeam(net.minecraft.server.v1_12_R1.Scoreboard(), player.displayName)
        team.setCanSeeFriendlyInvisibles(false)
        val packet = net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam(team, 0)
        packet.setProperty("h", listOf(player.displayName))
        onlinePlayers.forEach { p -> p.sendPacket(packet) }
        // endregion
    }

    private fun validateLineCount(line: Int): Int {
        if (uniqueOwner.size < line) error(
            """
                行数大于支持的最大行数。
                Lines size are larger than supported.
            """.t()
        )
        return line
    }

    /**
     * @param team 为\[content.size - line - 1\]
     */
    private fun sendTeamPrefixSuffix(player: Player, team: String, content: String) {
        if (MinecraftVersion.major >= 9) {
            val t = ScoreboardTeam(Scoreboard(), team)
            t.playerPrefix = component(content) as IChatBaseComponent
            player.sendPacket(
                PacketPlayOutScoreboardTeam::class.java.invokeConstructor(
                    team,
                    2,
                    Optional.of(PacketPlayOutScoreboardTeam.b(t)),
                    listOf(team)
                )
            )
            return
        }
        if (version >= 11300) {
            val t = net.minecraft.server.v1_16_R3.ScoreboardTeam(net.minecraft.server.v1_16_R3.Scoreboard(), team)
            t.prefix = component(content) as net.minecraft.server.v1_16_R3.IChatBaseComponent
            player.sendPacket(net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam(t, 2).apply {
                setProperty("h", listOf(team))
            })
            return
        }
        // region Legacy Version
        val t = net.minecraft.server.v1_12_R1.ScoreboardTeam(net.minecraft.server.v1_12_R1.Scoreboard(), team)
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
        t.prefix = prefix
        t.suffix = suffix
        val packet = net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam(t, 2)
        packet.setProperty("h", listOf(team))
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
                    player.sendPacket(
                        PacketPlayOutScoreboardScore::class.java.invokeConstructor(
                            uniqueOwner[i], objectiveName, i, Optional.empty<Any>(), Optional.empty<Any>()
                        )
                    )
                    return@forEach
                }
                // region Legacy Version
                // 1.20.4 改为 Record
                // String owner, String objectiveName, int score, @Nullable IChatBaseComponent display, @Nullable NumberFormat numberFormat
                if (MinecraftVersion.majorLegacy > 12002) {
                    player.sendPacket(
                        PacketPlayOutScoreboardScore::class.java.invokeConstructor(
                            uniqueOwner[i],
                            objectiveName,
                            i,
                            null,
                            null
                        )
                    )
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
                val score = ScoreboardScore(
                    net.minecraft.server.v1_12_R1.Scoreboard(), net.minecraft.server.v1_12_R1.ScoreboardObjective(
                        net.minecraft.server.v1_12_R1.Scoreboard(),
                        objectiveName,
                        net.minecraft.server.v1_12_R1.IScoreboardCriteria.i
                    ), uniqueOwner[i]
                )
                score.score = i
                val packet = net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore(score)
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
                    player.sendPacket(
                        ClientboundResetScorePacket::class.java.invokeConstructor(
                            uniqueOwner[i],
                            objectiveName
                        )
                    )
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
                player.sendPacket(net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore(objectiveName))
                // endregion
            }
        }
    }

}
// endregion