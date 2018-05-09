package me.skymc.taboolib.player;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;

@SuppressWarnings("deprecation")
public class PlayerUtils {
	
	/**
	 * 获取目标方块
	 * 
	 * @param player 玩家
	 * @param max 最大视野
	 * @return
	 */
	public static Block getTargetBlock(Player player, int max) {
		HashSet<Byte> Byte = new HashSet<>();
		Byte.add((byte) 0);
		return player.getTargetBlock(Byte, max);
	}
	
	/**
	 * 重写数据
	 * 
	 * @param player 玩家
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
}
