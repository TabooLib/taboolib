package player
import org.bukkit.entity.Player
import kotlin.math.roundToInt

/**
 * 作者: 老廖
 * 时间: 2022/10/3
 *
 **/
object Experience {
    /**
     * 扣除玩家指定的经验值
     * @param player 目标玩家
     * @param exp 要扣除的值
     */
    fun Player.takeTotalExperiences(exp: Int) {
        val exp2 = getTotalExperiences()
        this.level = 0
        this.exp = 0.0f
        this.totalExperience = 0
        this.giveExp(exp2 - exp)
        if (!this.isOnline){
            this.saveData()
        }
    }

    /**
     * 给予玩家指定的经验值
     * @param player 目标玩家
     * @param exp 要给予的值
     */
    fun Player.giveTotalExperiences(exp: Int) {
        val exp2 = getTotalExperiences()
        this.level = 0
        this.exp = 0.0f
        this.totalExperience = 0
        this.giveExp(exp2 + exp)
        if (!this.isOnline){
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
     * @param player 目标玩家
     * @param exp 要设置的值
     */
    fun Player.setTotalExperiences(exp: Int) {
        this.level = 0
        this.exp = 0.0f
        this.totalExperience = 0
        this.giveExp(exp)
    }

    /**
     * 获取玩家的经验值
     * @param player 麻痹玩家
     * @return 经验值
     */
    fun Player.getTotalExperiences(): Int {
        var experience = (getExperienceAtLevel(this.level) * this.exp).roundToInt()
        var currentLevel = this.level
        while (currentLevel > 0) {
            currentLevel--
            experience += getExperienceAtLevel(currentLevel)
        }
        if (experience < 0) {
            experience = 0
        }
        return experience
    }

    private fun getExperienceAtLevel(level: Int): Int {
        if (level <= 15) {
            return (level shl 1) + 7
        }
        return if (level <= 30) {
            level * 5 - 38
        } else level * 9 - 158
    }
}