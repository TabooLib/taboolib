package io.izzel.taboolib.origin.cronus;

import io.izzel.taboolib.util.Strings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;

import java.util.Optional;

/**
 * @Author 坏黑
 * @Since 2019-05-11 15:42
 */
public class CronusUtils {

    public static void addItem(Player player, ItemStack item) {
        player.getInventory().addItem(item).values().forEach(e -> player.getWorld().dropItem(player.getLocation(), e));
    }

    public static String NonNull(String in) {
        return Strings.isBlank(in) ? "-" : in;
    }

    public static ItemStack NonNull(ItemStack itemStack) {
        return Optional.ofNullable(itemStack).orElse(new ItemStack(Material.STONE));
    }

    public static org.bukkit.inventory.ItemStack getUsingItem(Player player, Material material) {
        return player.getItemInHand().getType() == material ? player.getItemInHand() : player.getInventory().getItemInOffHand();
    }

    public static boolean next(int page, int size, int entry) {
        return size / (double) entry > page + 1;
    }

    public static boolean isInt(String in) {
        try {
            Integer.parseInt(in);
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static boolean isDouble(String in) {
        try {
            Double.parseDouble(in);
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static boolean isBoolean(String in) {
        try {
            Boolean.parseBoolean(in);
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static void setTotalExperience(Player player, int exp) {
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);
        int amount = exp;
        while (amount > 0) {
            int expToLevel = getExpAtLevel(player);
            amount -= expToLevel;
            if (amount >= 0) {
                player.giveExp(expToLevel);
            } else {
                amount += expToLevel;
                player.giveExp(amount);
                amount = 0;
            }
        }
    }

    private static int getExpAtLevel(Player player) {
        return getExpAtLevel(player.getLevel());
    }

    public static int getExpAtLevel(int level) {
        if (level <= 15) {
            return (2 * level) + 7;
        }
        if ((level >= 16) && (level <= 30)) {
            return (5 * level) - 38;
        }
        return (9 * level) - 158;

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

    public static int getTotalExperience(Player player) {
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

    public static int getExpUntilNextLevel(Player player) {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int nextLevel = player.getLevel();
        return getExpAtLevel(nextLevel) - exp;
    }

    public static long toMillis(String in) {
        long time = 0;
        StringBuilder current = new StringBuilder();
        for (String charAt : in.toLowerCase().split("")) {
            if (isInt(charAt)) {
                current.append(charAt);
            } else {
                switch (charAt) {
                    case "d":
                        time += NumberConversions.toInt(current.toString()) * 24L * 60L * 60L * 1000L;
                        break;
                    case "h":
                        time += NumberConversions.toInt(current.toString()) * 60L * 60L * 1000L;
                        break;
                    case "m":
                        time += NumberConversions.toInt(current.toString()) * 60L * 1000L;
                        break;
                    case "s":
                        time += NumberConversions.toInt(current.toString()) * 1000L;
                        break;
                }
                current = new StringBuilder();
            }
        }
        return time;
    }

}
