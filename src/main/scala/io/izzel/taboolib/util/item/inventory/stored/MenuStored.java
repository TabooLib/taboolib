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

/**
 * 可交互界面构建工具
 * 这里的可交互指的是界面重的物品可以被修改
 *
 * @Author sky
 * @Since 2019-12-03 13:24
 */
public abstract class MenuStored {

    protected Player player;

    public MenuStored(Player player) {
        this.player = player;
    }

    public void open() {
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
        return false;
    }

    public String getTitle() {
        return player.getName();
    }

    public int getRows() {
        return 1;
    }

    public void onClick(ClickEvent e) {
        if (e.getClickType() == ClickType.CLICK) {
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
     */
    public void onClose(InventoryCloseEvent e) {
    }

    /**
     * 当界面刷新时
     */
    public void refresh(Inventory inventory) {
    }

    /**
     * 当界面刷新时（异步）
     */
    public void refreshAsync(Inventory inventory) {
    }

    /**
     * 物品是否可以放入该位置
     */
    public boolean shouldIntoSlot(Inventory inventory, ItemStack item, int slot) {
        return false;
    }

    /**
     * 是否为有效的位置
     */
    public boolean isIntoSlot(Inventory inventory, ItemStack item, int slot) {
        return false;
    }

    /**
     * 获取界面中有效的位置
     * 用于 shift 点击时的自动装填
     */
    public int getIntoSlot(Inventory inventory, ItemStack item) {
        return -1;
    }

    /**
     * 物品存入界面
     */
    public void intoItem(Inventory inventory, ItemStack item, int slot) {
        inventory.setItem(slot, item);
    }

    /**
     * 是否存在物品
     */
    public boolean existsItem(Inventory inventory, int slot) {
        return false;
    }

    /**
     * 获取物品
     */
    public ItemStack getItem(Inventory inventory, int slot) {
        return inventory.getItem(slot);
    }
}
