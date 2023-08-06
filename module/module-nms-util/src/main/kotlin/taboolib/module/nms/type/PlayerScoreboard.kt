package taboolib.module.nms.type

import org.bukkit.entity.Player
import taboolib.module.nms.nmsScoreboard

/**
 * 玩家记分板信息缓存
 */
class PlayerScoreboard(val player: Player) {

    /** 当前标题 */
    private var currentTitle = ""

    /** 当前内容 */
    private val currentContent = HashMap<Int, String>()

    /** 当前前缀 */
    private var prefix = ""

    /** 当前后缀 */
    private var suffix = ""

    /** 当前队伍颜色 */
    private var color = ChatColorFormat.RESET

    /** 是否被删除 */
    var deleted = false

    /** 是否被创建 */
    var created = false

    init {
        nmsScoreboard.setupScoreboard(player, true)
        nmsScoreboard.display(player)
        sendTitle(currentTitle)
    }

    fun sendTitle(title: String) {
        if (currentTitle != title) {
            currentTitle = title
            nmsScoreboard.setDisplayName(player, title)
        }
    }

    fun sendContent(lines: List<String>) {
        if (deleted) {
            nmsScoreboard.setupScoreboard(player, false, currentTitle)
            nmsScoreboard.display(player)
        }
        deleted = nmsScoreboard.changeContent(player, lines, currentContent)
        currentContent.clear()
        currentContent.putAll(lines.mapIndexed { index, s -> index to s }.toMap())
    }

    fun setPrefix(prefix: String, target: Player?) {
        this.prefix = prefix
        this.created = true
        nmsScoreboard.updateTeam(player, prefix, suffix, color, !created, target)
    }

    fun clearPrefix(target: Player?) {
        this.prefix = ""
        this.created = true
        nmsScoreboard.updateTeam(player, "", suffix, color, !created, target)
    }

    fun setSuffix(suffix: String, target: Player?) {
        this.suffix = suffix
        this.created = true
        nmsScoreboard.updateTeam(player, prefix, suffix, color, !created, target)
    }

    fun clearSuffix(target: Player?) {
        this.suffix = ""
        this.created = true
        nmsScoreboard.updateTeam(player, prefix, "", color, !created, target)
    }
    
    fun setColor(color: ChatColorFormat, target: Player?) {
        this.color = color
        this.created = true
        nmsScoreboard.updateTeam(player, prefix, suffix, color, !created, target)
    }
}