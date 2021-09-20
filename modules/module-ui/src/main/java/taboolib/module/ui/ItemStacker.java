package taboolib.module.ui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import taboolib.common.Isolated;
import taboolib.platform.util.ItemModifierKt;

import java.util.ArrayList;
import java.util.List;

/**
 * 物品背包合并工具
 *
 * @author 坏黑
 * @since 2019-02-07 23:53
 */
@Isolated
public abstract class ItemStacker {

    public static final ItemStacker MINECRAFT = new ItemStacker() {

        @Override
        public int getMaxStackSize(ItemStack itemStack) {
            return itemStack.getMaxStackSize();
        }
    };

    abstract public int getMaxStackSize(ItemStack itemStack);

    /**
     * 从箱子里移动物品到玩家背包
     * 如果溢出则丢弃
     *
     * @param item   物品
     * @param player 玩家
     */
    public void moveItemFromChest(ItemStack item, Player player) {
        AddResult result = addItemAndMerge(item, player.getInventory(), new ArrayList<>());
        if (result.countOut > 0) {
            item.setAmount(result.countOut);
            if (!addItemAndSplit(item, player.getInventory(), 0, true)) {
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }
    }

    public boolean addItemAndSplit(ItemStack item, Inventory inventory, int start) {
        return addItemAndSplit(item, inventory, start, false);
    }

    /**
     * 添加并拆分，但不合并
     * 返回值为是否添加完成
     * <p>
     *
     * @param item      物品
     * @param desc      快捷栏逆向添加，用于工作台拟真，会忽略 start 参数
     * @param inventory 背包
     * @param start     起始位置
     * @return boolean
     */
    public boolean addItemAndSplit(ItemStack item, Inventory inventory, int start, boolean desc) {
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

    public boolean addItemFromChestToPlayer(ItemStack item, Inventory inventory) {
        for (int i = 8; i >= 0; i--) {
            if (ItemModifierKt.isAir(inventory.getItem(i))) {
                if (item.getAmount() > getMaxStackSize(item)) {
                    ItemStack itemClone = item.clone();
                    itemClone.setAmount(getMaxStackSize(item));
                    inventory.setItem(i, itemClone);
                    item.setAmount(item.getAmount() - getMaxStackSize(item));
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
            if (ItemModifierKt.isAir(inventory.getItem(i))) {
                if (item.getAmount() > getMaxStackSize(item)) {
                    ItemStack itemClone = item.clone();
                    itemClone.setAmount(getMaxStackSize(item));
                    inventory.setItem(i, itemClone);
                    item.setAmount(item.getAmount() - getMaxStackSize(item));
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
     *
     * @param inventory 背包
     * @param item      物品
     * @param ignore    忽略位置
     * @return {@link AddResult}
     */
    @SuppressWarnings("ConstantConditions")
    public AddResult addItemAndMerge(ItemStack item, Inventory inventory, List<Integer> ignore) {
        boolean changed = false;
        int count = item.getAmount();
        int size = inventory instanceof PlayerInventory || inventory instanceof CraftingInventory ? 36 : inventory.getSize();
        for (int i = 0; i < size; i++) {
            if (ignore.contains(i)) {
                continue;
            }
            ItemStack inventoryItem = inventory.getItem(i);
            if (!item.isSimilar(inventoryItem)) {
                continue;
            }
            while (count > 0 && inventoryItem.getAmount() < getMaxStackSize(item)) {
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

    private boolean check(ItemStack item, Inventory inventory, int i) {
        if (ItemModifierKt.isAir(inventory.getItem(i))) {
            // 如果物品数量过多
            if (item.getAmount() > getMaxStackSize(item)) {
                ItemStack itemClone = item.clone();
                itemClone.setAmount(getMaxStackSize(item));
                inventory.setItem(i, itemClone);
                item.setAmount(item.getAmount() - getMaxStackSize(item));
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
