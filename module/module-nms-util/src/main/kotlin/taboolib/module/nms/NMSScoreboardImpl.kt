package taboolib.module.nms

import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import org.tabooproject.reflex.Reflex.Companion.unsafeInstance
import taboolib.module.nms.type.ChatColorFormat
import taboolib.platform.BukkitPlugin
import taboolib.platform.util.hasMeta
import taboolib.platform.util.onlinePlayers
import taboolib.platform.util.setMeta
import java.util.*

class NMSScoreboardImpl : NMSScoreboard() {

    fun component(text: String): Any {
        return if (MinecraftVersion.major >= 11) {
            IChatBaseComponent::class.java.invokeMethod<Any>("literal", text, isStatic = true)!!
        } else {
            ChatComponentText(text)
        }
    }

    override fun setupScoreboard(player: Player, color: Boolean, title: String) {
        val packet = PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.setProperty("objectiveName", key)
            packet.setProperty("displayName", component(title))
            packet.setProperty("renderType", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
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
            val packet = PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
            if (MinecraftVersion.isUniversal) {
                packet.setProperty("objectiveName", key)
                packet.setProperty("displayName", component("ScoreBoard"))
                packet.setProperty("renderType", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
                packet.setProperty("method", 1)
            } else {
                packet.setProperty("a", key)
                if (MinecraftVersion.major >= 5) {
                    packet.setProperty("b", component("ScoreBoard"))
                } else {
                    packet.setProperty("b", "ScoreBoard")
                }
                packet.setProperty("c", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
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
                sendTeamPrefixSuffix(player, uniqueColors[content.size - line - 1], ct)
            }
        }
        return false
    }

    override fun display(player: Player) {
        val packet = PacketPlayOutScoreboardDisplayObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.setProperty("slot", 1)
            packet.setProperty("objectiveName", key)
        } else {
            packet.setProperty("a", 1)
            packet.setProperty("b", key)
        }
        player.sendPacket(packet)
    }

    override fun setDisplayName(player: Player, title: String) {
        val packet = PacketPlayOutScoreboardObjective::class.java.unsafeInstance()
        if (MinecraftVersion.isUniversal) {
            packet.setProperty("objectiveName", key)
            packet.setProperty("displayName", component(title))
            packet.setProperty("renderType", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
            packet.setProperty("method", 2)
        } else {
            handle2DuplicatedPacket(packet, title)
            packet.setProperty("d", 2)
        }
        player.sendPacket(packet)
    }

    /**
     *
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
     *  If 2 the team team information is updated.
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
        uniqueColors.forEach { color ->
            if (MinecraftVersion.isUniversal) {
                val packet = PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
                packet.setProperty("method", 0)
                packet.setProperty("name", color)
                packet.setProperty("players", listOf(color))
                val b = universalTeamData.unsafeInstance()
                b.setProperty("displayName", component(color))
                if (MinecraftVersion.major >= 11) { // 1.19 "unexpected null component"
                    b.setProperty("playerPrefix", IChatBaseComponent.empty())
                    b.setProperty("playerSuffix", IChatBaseComponent.empty())
                }
                handle1DuplicatedPacket(b, packet, player, EnumChatFormat.RESET)
                return@forEach
            }
            if (MinecraftVersion.major >= 5) {
                val packet = PacketPlayOutScoreboardTeam()
                packet.setProperty("a", color)
                packet.setProperty("b", component(color))
                packet.setProperty("e", "always")
                packet.setProperty("f", "always")
                packet.setProperty("g", EnumChatFormat.RESET)
                packet.setProperty("h", listOf(color))
                packet.setProperty("i", 0)
                packet.setProperty("j", -1)
                player.sendPacket(packet)
                return@forEach
            }
            val packet = PacketPlayOutScoreboardTeam()
            packet.setProperty("a", color)
            packet.setProperty("b", color)
            packet.setProperty("e", ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS.e)
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
            val packet = PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
            packet.setProperty("method", 0)
            packet.setProperty("name", player.displayName)
            packet.setProperty("players", listOf(player.name))
            val b = universalTeamData.unsafeInstance()
            b.setProperty("displayName", component(player.displayName))
            handle1DuplicatedPacketAll(b, packet, EnumChatFormat.RESET)
            return
        }
        if (MinecraftVersion.major >= 5) {
            val packet = PacketPlayOutScoreboardTeam()
            packet.setProperty("a", player.displayName)
            packet.setProperty("b", component(player.displayName))
            packet.setProperty("e", "always")
            packet.setProperty("f", "always")
            packet.setProperty("g", EnumChatFormat.RESET)
            packet.setProperty("h", listOf(player.displayName))
            packet.setProperty("i", 0)
            packet.setProperty("j", -1)
            onlinePlayers.forEach { p -> p.sendPacket(packet) }
            return
        }
        val packet = PacketPlayOutScoreboardTeam()
        packet.setProperty("a", player.displayName)
        packet.setProperty("b", player.displayName)
        packet.setProperty("e", ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS.e)
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

    /**
     *
     * player -> 需要设置前缀或后缀的玩家
     * p -> 向该玩家发包,如果为Null则为全体发包
     *
     * @see EnumChatFormat
     * @see PacketPlayOutScoreboardTeam
     */
    override fun updateTeam(
        player: Player, prefix: String, suffix: String, color: ChatColorFormat, created: Boolean, target: Player?
    ) {
        if (created) {
            createTeam(player)
        }
        if (MinecraftVersion.isUniversal) {
            val packet = PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
            packet.setProperty("method", 2)
            packet.setProperty("name", player.displayName)
            val b = universalTeamData.unsafeInstance()
            b.setProperty("displayName", component(player.displayName))
            b.setProperty("playerPrefix", component(prefix))
            b.setProperty("playerSuffix", component(suffix))
            if (target == null) {
                handle1DuplicatedPacketAll(b, packet, EnumChatFormat.valueOf(color.name.uppercase()))
            } else {
                handle1DuplicatedPacket(b, packet, target, EnumChatFormat.valueOf(color.name.uppercase()))
            }
            return
        }
        if (MinecraftVersion.major >= 5) {
            val packet = PacketPlayOutScoreboardTeam()
            packet.setProperty("a", player.displayName)
            packet.setProperty("c", component(prefix))
            packet.setProperty("d", component(suffix))
            packet.setProperty("i", 2)
            if (target == null) {
                onlinePlayers.forEach { pp -> pp.sendPacket(packet) }
            } else target.sendPacket(packet)
            return
        }
        val packet = PacketPlayOutScoreboardTeam()
        packet.setProperty("a", player.displayName)
        packet.setProperty("c", prefix)
        packet.setProperty("d", suffix)
        if (target == null) {
            onlinePlayers.forEach { pp -> pp.sendPacket(packet) }
        } else target.sendPacket(packet)
    }

    private fun validateLineCount(line: Int) {
        if (uniqueColors.size < line) {
            throw IllegalArgumentException("Lines size are larger than supported.")
        }
    }

    /**
     * @param team 为\[content.size - line - 1\]
     */
    private fun sendTeamPrefixSuffix(player: Player, team: String, content: String) {
        if (MinecraftVersion.major >= 9) {
            val packet = PacketPlayOutScoreboardTeam::class.java.unsafeInstance()
            packet.setProperty("method", 2)
            packet.setProperty("name", team)
            packet.setProperty("players", listOf(team))
            val b = universalTeamData.unsafeInstance()
            b.setProperty("displayName", component(team))
            b.setProperty("playerPrefix", component(content))
            if (MinecraftVersion.major >= 11) { // 1.19 "unexpected null component"
                b.setProperty("playerSuffix", IChatBaseComponent.empty())
            }
            handle1DuplicatedPacket(b, packet, player, EnumChatFormat.RESET)
            return
        }
        if (MinecraftVersion.major >= 5) {
            val packet = PacketPlayOutScoreboardTeam()
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
            val color = ChatColor.getLastColors(prefix)
            suffix = color + content.substring(16 until content.length)
            if (suffix.length > 16) {
                suffix = suffix.substring(0, 16)
            }
        }
        val packet = PacketPlayOutScoreboardTeam()
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
                    val packet = PacketPlayOutScoreboardScore::class.java.unsafeInstance()
                    packet.setProperty("owner", uniqueColors[i])
                    packet.setProperty("objectiveName", key)
                    packet.setProperty("score", i)
                    packet.setProperty("method", ScoreboardServer.Action.CHANGE)
                    player.sendPacket(packet)
                    return@forEach
                }
                if (MinecraftVersion.major >= 5) {
                    val packet = PacketPlayOutScoreboardScore()
                    packet.setProperty("a", uniqueColors[i])
                    packet.setProperty("b", key)
                    packet.setProperty("c", i)
                    packet.setProperty("d", ScoreboardServer.Action.CHANGE)
                    player.sendPacket(packet)
                    return@forEach
                }
                val packet = PacketPlayOutScoreboardScore()
                packet.setProperty("a", uniqueColors[i])
                packet.setProperty("b", key)
                packet.setProperty("c", i)
                packet.setProperty(
                    "d", net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE
                )
                player.sendPacket(packet)
            }
        } else {
            (line until lastLineCount).forEach { i ->
                if (MinecraftVersion.major >= 9) {
                    val packet = PacketPlayOutScoreboardScore::class.java.unsafeInstance()
                    packet.setProperty("owner", uniqueColors[i])
                    packet.setProperty("objectiveName", key)
                    packet.setProperty("method", ScoreboardServer.Action.REMOVE)
                    player.sendPacket(packet)
                    return@forEach
                }
                if (MinecraftVersion.major >= 5) {
                    val packet = PacketPlayOutScoreboardScore()
                    packet.setProperty("a", uniqueColors[i])
                    packet.setProperty("b", key)
                    packet.setProperty("d", ScoreboardServer.Action.REMOVE)
                    player.sendPacket(packet)
                    return@forEach
                }
                val packet = PacketPlayOutScoreboardScore()
                packet.setProperty("a", uniqueColors[i])
                packet.setProperty("b", key)
                packet.setProperty(
                    "d", net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore.EnumScoreboardAction.REMOVE
                )
                player.sendPacket(packet)
            }
        }
    }

    private fun handle1DuplicatedPacket(b: Any, packet: Any, player: Player, color: EnumChatFormat) {
        b.setProperty("nametagVisibility", "always")
        b.setProperty("collisionRule", "always")
        b.setProperty("color", color)
        b.setProperty("options", 3)
        packet.setProperty("parameters", Optional.of(b))
        player.sendPacket(packet)
    }

    private fun handle1DuplicatedPacketAll(b: Any, packet: Any, color: EnumChatFormat) {
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
        packet.setProperty("a", key)
        if (MinecraftVersion.major >= 5) {
            packet.setProperty("b", component(title))
        } else {
            packet.setProperty("b", title)
        }
        packet.setProperty("c", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER)
    }
}