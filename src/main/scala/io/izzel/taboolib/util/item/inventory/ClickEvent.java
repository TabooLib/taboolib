package io.izzel.taboolib.util.item.inventory;

import com.google.common.collect.Lists;
import io.izzel.taboolib.util.lite.Servers;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 界面构建工具 {@link MenuBuilder} 中的点击事件
 *
 * @Author 坏黑
 * @Since 2019-05-21 18:09
 */
public class ClickEvent {

    private final ClickType clickType;
    private final Event event;
    private final char slot;

    public ClickEvent(ClickType clickType, Event event, char slot) {
        this.clickType = clickType;
        this.event = event;
        this.slot = slot;
    }

    @NotNull
    public ClickEvent onClick(Consumer<InventoryClickEvent> consumer) {
        consumer.accept(castClick());
        return this;
    }

    @NotNull
    public ClickEvent onDrag(Consumer<InventoryDragEvent> consumer) {
        consumer.accept(castDrag());
        return this;
    }

    /**
     * 获取受影响的物品
     */
    @NotNull
    public List<ItemStack> getAffectItems() {
        return clickType == ClickType.CLICK ? Servers.getAffectItemInClickEvent((InventoryClickEvent) event) : Lists.newArrayList();
    }

    /**
     * 转换为点击事件
     */
    @NotNull
    public InventoryClickEvent castClick() {
        return (InventoryClickEvent) event;
    }

    /**
     * 转换为拖动事件
     */
    @NotNull
    public InventoryDragEvent castDrag() {
        return (InventoryDragEvent) event;
    }

    /**
     * 获取点击位置
     */
    public int getRawSlot() {
        return clickType == ClickType.CLICK ? castClick().getRawSlot() : -1;
    }

    /**
     * 获取点击位置（文本映射类型）
     */
    public char getSlot() {
        return slot;
    }

    /**
     * 获取点击方式
     */
    public ClickType getClickType() {
        return clickType;
    }

    /**
     * 获取点击者（玩家）实例
     */
    public Player getClicker() {
        return (Player) ((InventoryInteractEvent) event).getWhoClicked();
    }

    /**
     * 获取背包实例
     */
    public Inventory getInventory() {
        return ((InventoryEvent) event).getInventory();
    }

    /**
     * 是否取消点击事件
     */
    public void setCancelled(boolean c) {
        ((Cancellable) event).setCancelled(true);
    }

    /**
     * 点击事件是否被取消
     */
    public boolean isCancelled() {
        return ((Cancellable) event).isCancelled();
    }

    /**
     * 获取点击物品（仅限 CLICK 点击方式）
     */
    @Nullable
    public ItemStack getCurrentItem() {
        return clickType == ClickType.CLICK ? castClick().getCurrentItem() : null;
    }

    /**
     * 设置点击物品（仅限 CLICK 点击方式）
     * @param item 物品实例
     */
    public void setCurrentItem(ItemStack item) {
        if (clickType == ClickType.CLICK) {
            castClick().setCurrentItem(item);
        }
    }
}
