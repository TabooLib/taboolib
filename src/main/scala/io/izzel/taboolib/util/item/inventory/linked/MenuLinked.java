package io.izzel.taboolib.util.item.inventory.linked;

import io.izzel.taboolib.kotlin.Indexed;
import io.izzel.taboolib.util.item.inventory.ClickEvent;
import io.izzel.taboolib.util.item.inventory.MenuBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多页列表界面构建工具
 * 这里的界面中的物品不可被修改
 *
 * @author bkm016
 * @since 2020/11/17 1:26 上午
 */
public abstract class MenuLinked<T> {

    public void open(Player player, int page) {
        Map<Integer, T> objectsMap = new HashMap<>();
        List<Integer> slots = getSlots();
        List<T> itemsAll = getElements();
        List<T> items = Indexed.INSTANCE.subList(itemsAll, page * slots.size(), (page + 1) * slots.size() - 1);
        MenuBuilder.builder()
                .lockHand(isLockHand())
                .title(getTitle())
                .rows(getRows())
                .build(inventory -> {

                })
                .click(e -> {

                }).open(player);
    }

    public boolean isLockHand() {
        return false;
    }

    public String getTitle() {
        return "MenuLinked";
    }

    public int getRows() {
        return 1;
    }

    /**
     * 获取所有元素
     */
    abstract public List<T> getElements();

    /**
     * 获取界面中可存放元素的格子
     */
    abstract public List<Integer> getSlots();

    /**
     * 当页面即将构建完成时
     */
    abstract public void onBuild(@NotNull Inventory inventory);

    /**
     * 当玩家进行元素点击动作时
     *
     * @param event   点击事件
     * @param element 点击元素
     */
    abstract public void onClick(@NotNull ClickEvent event, @NotNull T element);
}
