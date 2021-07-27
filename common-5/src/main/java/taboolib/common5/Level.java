package taboolib.common5;

import taboolib.common.Isolated;
import taboolib.common.platform.FunctionKt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.ProxyPlayer;

/**
 * TabooLib
 * taboolib.common5.Level
 *
 * @author sky
 * @since 2021/7/5 12:11 上午
 */
@Isolated
public class Level {

    public static void setTotalExperience(ProxyPlayer player, int exp) {
        player.setExp(0);
        player.setLevel(0);
        int amount = exp;
        while (amount > 0) {
            int expToLevel = getExpAtLevel(player);
            amount -= expToLevel;
            if (amount >= 0) {
                player.setExp(player.getExp() + expToLevel);
            } else {
                amount += expToLevel;
                player.setExp(player.getExp() + amount);
                amount = 0;
            }
        }
    }

    private static int getExpAtLevel(ProxyPlayer player) {
        return getExpAtLevel(player.getLevel());
    }

    public static int getExpAtLevel(int level) {
        if (FunctionKt.getRunningPlatform() == Platform.NUKKIT) {
            if (level >= 30) {
                return 112 + (level - 30) * 9;
            } else if (level >= 15) {
                return 37 + (level - 15) * 5;
            } else {
                return 7 + level * 2;
            }
        } else {
            if (level <= 15) {
                return (2 * level) + 7;
            } else if (level <= 30) {
                return (5 * level) - 38;
            } else {
                return (9 * level) - 158;
            }
        }
    }

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

    public static int getExpUntilNextLevel(ProxyPlayer player) {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int nextLevel = player.getLevel();
        return getExpAtLevel(nextLevel) - exp;
    }
}
