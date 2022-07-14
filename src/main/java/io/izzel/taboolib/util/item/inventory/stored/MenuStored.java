package io.izzel.taboolib.util.item.inventory.stored;

import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.item.inventory.ClickEvent;
import io.izzel.taboolib.util.item.inventory.ClickType;
import io.izzel.taboolib.util.item.inventory.MenuBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 可交互界面构建工具
 * 这里的可交互指的是界面中的物品可以被修改
 *
 * @author sky
 * @since 2019-12-03 13:24
 */
public abstract class MenuStored {

    protected Player player;

    public MenuStored() {
    }

    public MenuStored(Player player) {
        this.player = player;
    }

    public void open() {
        open(player);
    }

    public void open(Player player) {
        MenuBuilder.builder()
                .lockHand(isLockHand())
                .title(getTitle())
                .rows(getRows())
                .event(this::onClick)
                .close(this::onClose)
                .build(this::refresh)
                .buildAsync(this::refreshAsync)
                .open(player);
    }

    public boolean isLockHand() {
        return true;
    }

    public String getTitle() {
        return "MenuStored";
    }

    public int getRows() {
        return 1;
    }

    public void onClick(@NotNull ClickEvent e) {
        if (e.getClickType() == ClickType.DRAG) {
            for (int slot : e.castDrag().getRawSlots()) {
                if (slot < e.castDrag().getInventory().getSize()) {
                    e.setCancelled(true);
                }
            }
        }
        if (e.getClickType() == ClickType.CLICK) {
            onSingleClick(e);
        }
    }

    public void onSingleClick(@NotNull ClickEvent e) {
        // 自动装填
        if (e.castClick().getClick().isShiftClick() && e.getRawSlot() >= e.getInventory().getSize() && Items.nonNull(e.getCurrentItem())) {
            e.setCancelled(true);
            // 获取有效位置
            int validSlot = getIntoSlot(e.getInventory(), e.getCurrentItem());
            if (validSlot >= 0) {
                // 设置物品
                intoItem(e.getInventory(), e.getCurrentItem(), validSlot);
                // 移除物品
                e.setCurrentItem(null);
                onClicked();
            }
        }
        // 手动装填
        else {
            // todo 合并物品
            if (e.castClick().getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                e.setCancelled(true);
                return;
            }
            Action action;
            if (e.castClick().getClick().isShiftClick() && e.getRawSlot() >= 0 && e.getRawSlot() < e.getInventory().getSize()) {
                action = new ActionQuickTake();
            } else if (e.castClick().getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY) {
                action = new ActionKeyboard();
            } else {
                action = new ActionClick();
            }
            // 点击有效位置
            if (isIntoSlot(e.getInventory(), action.getCurrent(e), action.getCurrentSlot(e))) {
                e.setCancelled(true);
                // 提取动作
                if (Items.isNull(action.getCurrent(e)) && existsItem(e.getInventory(), action.getCurrentSlot(e))) {
                    // 提取物品
                    action.setCurrent(e, getItem(e.getInventory(), action.getCurrentSlot(e)));
                    // 删除物品
                    intoItem(e.getInventory(), null, action.getCurrentSlot(e));
                    onClicked();
                }
                // 合法的位置
                else if (shouldIntoSlot(e.getInventory(), action.getCurrent(e), action.getCurrentSlot(e))) {
                    ItemStack current = action.getCurrent(e);
                    // 提取物品
                    action.setCurrent(e, getItem(e.getInventory(), action.getCurrentSlot(e)));
                    // 写入物品
                    intoItem(e.getInventory(), current, action.getCurrentSlot(e));
                    onClicked();
                }
            }
            // 点击无效位置
            else if (e.getRawSlot() >= 0 && e.getRawSlot() < e.getInventory().getSize()) {
                e.setCancelled(true);
            }
        }
    }

    /**
     * 当点击动作完成时
     */
    public void onClicked() {
    }

    /**
     * 当界面关闭时
     *
     * @param inventoryCloseEvent 事件
     */
    public void onClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
    }

    /**
     * 当界面刷新时
     *
     * @param inventory 背包
     */
    public void refresh(@NotNull Inventory inventory) {
    }

    /**
     * 当界面刷新时（异步）
     *
     * @param inventory 背包
     */
    public void refreshAsync(@NotNull Inventory inventory) {
    }

    /**
     * 物品是否可以放入该位置
     *
     * @param inventory 背包
     * @param slot      位置
     * @param item      物品
     * @return boolean
     */
    public boolean shouldIntoSlot(@NotNull Inventory inventory, @NotNull ItemStack item, int slot) {
        return false;
    }

    /**
     * 是否为有效的位置
     *
     * @param item      物品
     * @param slot      位置
     * @param inventory 背包
     * @return boolean
     */
    public boolean isIntoSlot(@NotNull Inventory inventory, @NotNull ItemStack item, int slot) {
        return false;
    }

    /**
     * 获取界面中有效的位置
     * 用于 shift 点击时的自动装填
     *
     * @param item      物品
     * @param inventory 背包
     * @return 位置
     */
    public int getIntoSlot(@NotNull Inventory inventory, @NotNull ItemStack item) {
        return -1;
    }

    /**
     * 物品存入界面
     *
     * @param inventory 洁面
     * @param item      物品
     * @param slot      位置
     */
    public void intoItem(@NotNull Inventory inventory, @Nullable ItemStack item, int slot) {
        inventory.setItem(slot, item);
    }

    /**
     * @param inventory 界面
     * @param slot      物品
     * @return 是否存在物品
     */
    public boolean existsItem(@NotNull Inventory inventory, int slot) {
        return false;
    }

    /**
     * @param inventory 界面
     * @param slot      位置
     * @return 物品
     */
    public @Nullable ItemStack getItem(@NotNull Inventory inventory, int slot) {
        return inventory.getItem(slot);
    }
}