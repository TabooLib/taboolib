package me.skymc.taboolib.player;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author sky
 */
public class PlayerUtils {

    private static boolean setup;
    private static boolean useReflection;
    private static Method oldGetOnlinePlayersMethod;

    public static Collection<? extends Player> getOnlinePlayers() {
        try {
            if (!setup) {
                oldGetOnlinePlayersMethod = Bukkit.class.getDeclaredMethod("getOnlinePlayers");
                if (oldGetOnlinePlayersMethod.getReturnType() == Player[].class) {
                    useReflection = true;
                }
                setup = true;
            }
            if (!useReflection) {
                return Bukkit.getOnlinePlayers();
            } else {
                Player[] playersArray = (Player[]) oldGetOnlinePlayersMethod.invoke(null);
                return ImmutableList.copyOf(playersArray);
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取目标方块
     *
     * @param player 玩家
     * @param max    最大视野
     * @return
     */
    public static Block getTargetBlock(Player player, int max) {
        HashSet<Byte> bytes = new HashSet<>();
        bytes.add((byte) 0);
        return player.getTargetBlock(bytes, max);
    }

    /**
     * 重写数据
     *
     * @param player     玩家
     * @param scoreboard 是否清理计分板
     */
    public static void resetData(Player player, boolean scoreboard) {
        if (player.isDead()) {
            player.spigot().respawn();
        }
        player.closeInventory();
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setContents(new ItemStack[0]);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setExp(0.0F);
        player.setLevel(0);
        player.setSneaking(false);
        player.setSprinting(false);
        player.setFoodLevel(20);
        player.setSaturation(10.0F);
        player.setExhaustion(0.0F);
        player.setMaxHealth(20.0D);
        player.setHealth(20.0D);
        player.setFireTicks(0);
        player.setItemOnCursor(null);
        player.getActivePotionEffects().clear();
        player.getEnderChest().clear();
        player.updateInventory();
        if (scoreboard) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    /**
     * 获取玩家的鱼钩
     *
     * @param player 玩家
     * @return net.minecraft.server.{version}.EntityFishingHook
     */
    public static Object getPlayerHookedFish(HumanEntity player) {
        try {
            Object entityHuman = player.getClass().getMethod("getHandle").invoke(player);
            return entityHuman.getClass().getField("hookedFish").get(entityHuman);
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 获取鱼钩的钓鱼时间
     *
     * @param fishHook 鱼钩
     * @return int
     */
    public static int getFishingTicks(Object fishHook) {
        try {
            Field fishingTicks = fishHook.getClass().getDeclaredField("h");
            fishingTicks.setAccessible(true);
            return (int) fishingTicks.get(fishHook);
        } catch (Exception ignored) {
        }
        return -1;
    }

    /**
     * 设置鱼钩的钓鱼时间
     *
     * @param fishHook 鱼钩
     * @param ticks    时间
     */
    public static void setFishingTicks(Object fishHook, int ticks) {
        try {
            Field fishingTicks = fishHook.getClass().getDeclaredField("h");
            fishingTicks.setAccessible(true);
            fishingTicks.set(fishHook, ticks);
        } catch (Exception ignored) {
        }
    }
}
