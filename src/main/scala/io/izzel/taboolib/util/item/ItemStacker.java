package io.izzel.taboolib.util.item;

import io.izzel.taboolib.util.ArrayUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * 物品背包合并工具
 *
 * @Author 坏黑
 * @Since 2019-02-07 23:53
 */
public class ItemStacker {

    /**
     * 从箱子里移动物品到玩家背包
     * 如果溢出则丢弃
     */
    public static void moveItemFromChest(ItemStack item, Player player) {
        AddResult result = addItemAndMerge(item, player.getInventory(), new Integer[0]);
        if (result.countOut > 0) {
            item.setAmount(result.countOut);
            if (!addItemAndSplit(item, player.getInventory(), 0, true)) {
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }
    }

    public static boolean addItemAndSplit(ItemStack item, Inventory inventory, int start) {
        return addItemAndSplit(item, inventory, start, false);
    }

    /**
     * 添加并拆分，但不合并
     * 返回值为是否添加完成
     * <p>
     * desc = 快捷栏逆向添加，用于工作台拟真，会忽略 start 参数
     */
    public static boolean addItemAndSplit(ItemStack item, Inventory inventory, int start, boolean desc) {
        int size = inventory instanceof PlayerInventory || inventory instanceof CraftingInventory ? 36 : inventory.getSize();
        if (desc) {
            // 8 ~ 0
            for (int i = 8; i >= 0; i--) {
                if (check(item, inventory, i)) {
                    return true;
                }
            }
        }
        // 9 ~ 36
        for (int i = desc ? start + 9 : start; i < size; i++) {
            if (check(item, inventory, i)) {
                return true;
            }
        }
        return false;
    }

    public static boolean addItemFromChestToPlayer(ItemStack item, Inventory inventory) {
        for (int i = 8; i >= 0; i--) {
            if (Items.isNull(inventory.getItem(i))) {
                if (item.getAmount() > item.getType().getMaxStackSize()) {
                    ItemStack itemClone = item.clone();
                    itemClone.setAmount(item.getType().getMaxStackSize());
                    inventory.setItem(i, itemClone);
                    item.setAmount(item.getAmount() - item.getType().getMaxStackSize());
                } else {
                    ItemStack itemClone = item.clone();
                    itemClone.setAmount(item.getAmount());
                    inventory.setItem(i, itemClone);
                    item.setAmount(0);
                    return true;
                }
            }
        }
        for (int i = 35; i >= 9; i--) {
            if (Items.isNull(inventory.getItem(i))) {
                if (item.getAmount() > item.getType().getMaxStackSize()) {
                    ItemStack itemClone = item.clone();
                    itemClone.setAmount(item.getType().getMaxStackSize());
                    inventory.setItem(i, itemClone);
                    item.setAmount(item.getAmount() - item.getType().getMaxStackSize());
                } else {
                    ItemStack itemClone = item.clone();
                    itemClone.setAmount(item.getAmount());
                    inventory.setItem(i, itemClone);
                    item.setAmount(0);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 合并物品，不新增
     */
    public static AddResult addItemAndMerge(ItemStack item, Inventory inventory, Integer[] ignore) {
        boolean changed = false;
        int count = item.getAmount();
        int size = inventory instanceof PlayerInventory || inventory instanceof CraftingInventory ? 36 : inventory.getSize();
        for (int i = 0; i < size; i++) {
            if (ArrayUtil.contains(ignore, i)) {
                continue;
            }
            ItemStack inventoryItem = inventory.getItem(i);
            if (!item.isSimilar(inventoryItem)) {
                continue;
            }
            while (count > 0 && inventoryItem.getAmount() < item.getType().getMaxStackSize()) {
                changed = true;
                inventoryItem.setAmount(inventoryItem.getAmount() + 1);
                count--;
            }
            if (count == 0) {
                return new AddResult(count, changed);
            }
        }
        return new AddResult(count, changed);
    }

    private static boolean check(ItemStack item, Inventory inventory, int i) {
        if (Items.isNull(inventory.getItem(i))) {
            // 如果物品数量过多
            if (item.getAmount() > item.getType().getMaxStackSize()) {
                ItemStack itemClone = item.clone();
                itemClone.setAmount(item.getType().getMaxStackSize());
                inventory.setItem(i, itemClone);
                item.setAmount(item.getAmount() - item.getType().getMaxStackSize());
            } else {
                inventory.setItem(i, item.clone());
                item.setAmount(0);
                return true;
            }
        }
        return false;
    }

    public static class AddResult {

        private final int countOut;
        private final boolean changed;

        public AddResult(int countOut, boolean changed) {
            this.countOut = countOut;
            this.changed = changed;
        }

        public int getCountOut() {
            return countOut;
        }

        public boolean isChanged() {
            return changed;
        }

        @Override
        public String toString() {
            return "AddResult{" +
                    "countOut=" + countOut +
                    ", changed=" + changed +
                    '}';
        }
    }
}
