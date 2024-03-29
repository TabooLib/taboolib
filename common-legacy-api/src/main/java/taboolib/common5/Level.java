package taboolib.common5;

import taboolib.common.platform.ProxyPlayer;

/**
 * TabooLib
 * taboolib.common5.Level
 *
 * @author sky
 * @since 2021/7/5 12:11 上午
 */
public class Level {

    /**
     * 设置玩家的当前经验总值
     *
     * @param player 玩家
     * @param exp 经验
     */
    public static void setTotalExperience(ProxyPlayer player, int exp) {
        player.setLevel(0);
        player.setExp(0);
        player.giveExp(exp);
    }

    private static int getExpAtLevel(ProxyPlayer player) {
        return getExpAtLevel(player.getLevel());
    }

    /**
     * 获取当前等级下的最大经验
     * 根据运行平台分为 Bukkit 和 Nukkit 两种不同的算法
     *
     * @param level 等级
     */
    public static int getExpAtLevel(int level) {
        if (level <= 15) {
            return (2 * level) + 7;
        } else if (level <= 30) {
            return (5 * level) - 38;
        } else {
            return (9 * level) - 158;
        }
    }

    /**
     * 获取当前等级下的最大经验总值（从 1 级到该等级的所有最大经验总和）
     *
     * @param level 等级
     */
    public static int getExpToLevel(int level) {
        int currentLevel = 0;
        int exp = 0;
        while (currentLevel < level) {
            exp += getExpAtLevel(currentLevel);
            currentLevel++;
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    /**
     * 获取玩家当前经验总值
     *
     * @param player 玩家
     */
    public static int getTotalExperience(ProxyPlayer player) {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int currentLevel = player.getLevel();
        while (currentLevel > 0) {
            currentLevel--;
            exp += getExpAtLevel(currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    /**
     * 获取玩家距离升级还差多少经验
     *
     * @param player 玩家
     */
    public static int getExpUntilNextLevel(ProxyPlayer player) {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int nextLevel = player.getLevel();
        return getExpAtLevel(nextLevel) - exp;
    }
}
