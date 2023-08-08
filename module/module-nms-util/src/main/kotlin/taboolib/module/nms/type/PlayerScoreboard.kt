package taboolib.module.nms.type

import org.bukkit.entity.Player
import taboolib.module.nms.NMSScoreboard
import taboolib.module.nms.nmsProxy

/**
 * 玩家记分板信息缓存
 */
class PlayerScoreboard(val player: Player) {

    /** 记分板工具 */
    private val nmsScoreboard = nmsProxy<NMSScoreboard>()

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
        // 初始化记分板
        nmsScoreboard.setupScoreboard(player, true)
        // 展示记分板
        nmsScoreboard.display(player)
        // 设置标题
        sendTitle(currentTitle)
    }

    /**
     * 设置记分板标题
     */
    fun sendTitle(title: String) {
        if (currentTitle != title) {
            currentTitle = title
            nmsScoreboard.setDisplayName(player, title)
        }
    }

    /**
     * 设置记分板内容
     */
    fun sendContent(lines: List<String>) {
        // 如果记分板被删除，则重新创建记分板
        if (deleted) {
            nmsScoreboard.setupScoreboard(player, false, currentTitle)
            nmsScoreboard.display(player)
        }
        deleted = nmsScoreboard.changeContent(player, lines, currentContent)
        currentContent.clear()
        currentContent.putAll(lines.mapIndexed { index, s -> index to s }.toMap())
    }

    /**
     * 设置前缀
     */
    fun setPrefix(prefix: String, target: Player?) {
        this.prefix = prefix
        this.created = true
        nmsScoreboard.updateTeam(player, prefix, suffix, color, !created, target)
    }

    /**
     * 清空前缀
     */
    fun clearPrefix(target: Player?) {
        this.prefix = ""
        this.created = true
        nmsScoreboard.updateTeam(player, "", suffix, color, !created, target)
    }

    /**
     * 设置后缀
     */
    fun setSuffix(suffix: String, target: Player?) {
        this.suffix = suffix
        this.created = true
        nmsScoreboard.updateTeam(player, prefix, suffix, color, !created, target)
    }

    /**
     * 清空后缀
     */
    fun clearSuffix(target: Player?) {
        this.suffix = ""
        this.created = true
        nmsScoreboard.updateTeam(player, prefix, "", color, !created, target)
    }

    /**
     * 设置颜色
     */
    fun setColor(color: ChatColorFormat, target: Player?) {
        this.color = color
        this.created = true
        nmsScoreboard.updateTeam(player, prefix, suffix, color, !created, target)
    }
}