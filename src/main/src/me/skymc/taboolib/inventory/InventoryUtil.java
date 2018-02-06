package me.skymc.taboolib.inventory;

import java.util.Arrays;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.skymc.taboolib.methods.MethodsUtils;

public class InventoryUtil {
	
	public final static LinkedList<Integer> SLOT_OF_CENTENTS = new LinkedList<>(Arrays.asList(
			10, 11, 12, 13, 14, 15, 16,
			19, 20, 21, 22, 23, 24, 25,
			28, 29, 30, 31, 32, 33, 34,
			37, 38, 39, 40, 41, 42, 43));
	
	@Deprecated
	public static boolean isEmpey(Player p) {
        return isEmpty(p, 0);
    }
	
	/**
	 * 检查背包是否有空位
	 * 
	 * @param p 玩家
	 * @param i 起始位置
	 */
	public static boolean isEmpty(Player p, int i) {
        while (i < 35) {
            if (p.getInventory().getItem(i) == null) {
            	return true;
            }
            i++;
        }
        return false;
    }
	
	/**
	 * 检测玩家是否有指定物品
	 * 
	 * @param player 玩家
	 * @param item 物品
	 * @param amount 数量
	 * @param remove 是否删除
	 */
	public static boolean hasItem(Player player, ItemStack item, int amount, boolean remove) {
		int hasAmount = 0;
		for (ItemStack _item : player.getInventory()) {
			if (item.isSimilar(_item)) {
				hasAmount += _item.getAmount();
			}
		}
		if (hasAmount < amount) {
			return false;
		}
		int requireAmount = amount;
		for (int i = 0; i < player.getInventory().getSize() && remove; i++) {
			ItemStack _item = player.getInventory().getItem(i);
			if (_item != null && _item.isSimilar(item)) {
				/**
				 * 如果循环到的物品数量 小于 需要的数量
				 * 则 删除物品，减少需要的数量
				 */
				if (_item.getAmount() < requireAmount) {
					player.getInventory().setItem(i, null);
					requireAmount -= _item.getAmount();
				}
				/**
				 * 如果循环到的物品数量 等于 需要的数量
				 * 则 删除物品，直接结束
				 */
				else if (_item.getAmount() == requireAmount) {
					player.getInventory().setItem(i, null);
					return true;
				}
				/**
				 * 如果循环到的物品数量 大于 需要的数量
				 * 则扣除 需要的数量
				 */
				else {
					_item.setAmount(_item.getAmount() - requireAmount);
					return true;
				}
			}
		}
		return true;
	}
	
	@Deprecated
	public static boolean hasItem(Inventory targetInventory, ItemStack targetItem, Integer amount) {
		int inventoryAmount = 0;
		for (ItemStack item : targetInventory) {
			if (item != null) {
				if (item.isSimilar(targetItem)) {
					inventoryAmount += item.getAmount();
				}
			}
		}
		if (inventoryAmount >= amount) {
			return true;
		}
		return false;
	}
	
	@Deprecated
	public static boolean takeItem2(Inventory inv, ItemStack takeitem, Integer amount) {
		for (int i = 0; i < inv.getSize(); ++i) {
			if (amount <= 0) {
				return true;
			}
			
			ItemStack item = inv.getItem(i);
			if (item == null) {
				continue;
			}
			if (!item.isSimilar(takeitem)) {
				continue;
			}
			if (item.getAmount() >= amount) {
				if (item.getAmount() - amount == 0) {
					inv.setItem(i, null);
				}
				else {
					item.setAmount(item.getAmount() - amount);
				}
				return true;
			}
			else {
				amount -= item.getAmount();
				inv.setItem(i, null);
			}
		}
		return false;
	}
}
