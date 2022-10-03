package taboolib.expansion.geek

import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.common5.Level

/**
 * 扣除玩家指定的经验值
 * @param exp 要扣除的值
 */
fun Player.takeTotalExperiences(exp: Int) {
    val exp2 = getTotalExperiences()
    this.level = 0
    this.exp = 0.0f
    this.totalExperience = 0
    this.giveExp(exp2 - exp)
    if (!this.isOnline) {
        this.saveData()
    }
}

/**
 * 给予玩家指定的经验值
 * @param exp 要给予的值
 */
fun Player.giveTotalExperiences(exp: Int) {
    val exp2 = getTotalExperiences()
    this.level = 0
    this.exp = 0.0f
    this.totalExperience = 0
    this.giveExp(exp2 + exp)
    if (!this.isOnline) {
        this.saveData()
    }
}

fun Player.hasTotalExperiences(exp: Int, take: Boolean = false): Boolean {
    if (getTotalExperiences() >= exp) {
        if (take) {
            takeTotalExperiences(exp)
        }
        return true
    }
    return false
}

/**
 * 设置玩家经验值为指定值
 * @param exp 要设置的值
 */
fun Player.setTotalExperiences(exp: Int) {
    Level.setTotalExperience(adaptPlayer(this), exp)
}

/**
 * 获取玩家的经验值
 * @return 经验值
 */
fun Player.getTotalExperiences(): Int {
    return Level.getTotalExperience(adaptPlayer(this))
}