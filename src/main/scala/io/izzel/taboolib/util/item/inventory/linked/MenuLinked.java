package io.izzel.taboolib.util.item.inventory.linked;

import com.google.common.collect.Maps;
import io.izzel.taboolib.cronus.CronusUtils;
import io.izzel.taboolib.kotlin.Indexed;
import io.izzel.taboolib.util.item.inventory.ClickEvent;
import io.izzel.taboolib.util.item.inventory.MenuBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 多页列表界面构建工具
 * 这里的界面中的物品不可被修改
 *
 * @author bkm016
 * @since 2020/11/17 1:26 上午
 */
public abstract class MenuLinked<T> {

    protected final Player player;
    protected final Map<Integer, Consumer<ClickEvent>> buttonMap = Maps.newHashMap();
    protected Map<Integer, T> objectsMap;
    protected List<Integer> slots;
    protected List<T> itemsAll;
    protected List<T> items;
    protected int page = 0;

    public MenuLinked(Player player) {
        this.player = player;
    }

    public void open() {
        this.open(0);
    }

    public void open(int page) {
        this.page = page;
        this.objectsMap = new HashMap<>();
        this.slots = getSlots();
        this.itemsAll = getElements();
        this.items = Indexed.INSTANCE.subList(itemsAll, page * slots.size(), (page + 1) * slots.size() - 1);
        MenuBuilder.builder()
                .lockHand(isLockHand())
                .title(getTitle())
                .rows(getRows())
                .buildAsync(this::onBuildAsync)
                .build(inventory -> {
                    for (int i = 0; i < items.size(); i++) {
                        objectsMap.put(slots.get(i), items.get(i));
                        inventory.setItem(slots.get(i), generateItem(player, items.get(i), i, slots.get(i)));
                    }
                    onBuild(inventory);
                })
                .click(e -> {
                    if (objectsMap.containsKey(e.getRawSlot())) {
                        onClick(e, objectsMap.get(e.getRawSlot()));
                    } else if (buttonMap.containsKey(e.getRawSlot())) {
                        buttonMap.get(e.getRawSlot()).accept(e);
                    }
                })
                .close(this::onClose)
                .open(player);
    }

    /**
     * 添加界面交互按钮
     *
     * @param slot  位置
     * @param event 事件
     */
    public void addButton(int slot, Consumer<ClickEvent> event) {
        buttonMap.put(slot, event);
    }

    /**
     * 添加界面交互按钮（下一页）
     */
    public void addButtonNextPage(int slot) {
        addButton(slot, e -> {
            if (hasNextPage()) {
                open(page + 1);
            }
        });
    }

    /**
     * 添加界面交互按钮（上一页）
     */
    public void addButtonPreviousPage(int slot) {
        addButton(slot, e -> {
            if (hasPreviousPage()) {
                open(page - 1);
            }
        });
    }

    /**
     * 是否有上一页
     */
    public boolean hasPreviousPage() {
        return page > 0;
    }

    /**
     * 是否有下一页
     */
    public boolean hasNextPage() {
        return CronusUtils.next(page, itemsAll.size(), slots.size());
    }

    /**
     * 是否锁定手持物品
     */
    public boolean isLockHand() {
        return true;
    }

    /**
     * 页面标题
     */
    public String getTitle() {
        return "MenuLinked";
    }

    /**
     * 页面行数
     */
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

    /**
     * 生成元素所对应的物品
     *
     * @param player  玩家
     * @param element 元素
     * @param index   序号
     * @param slot    位置
     */
    abstract public ItemStack generateItem(@NotNull Player player, @NotNull T element, int index, int slot);

    /**
     * 当界面关闭时
     */
    public void onClose(@NotNull InventoryCloseEvent e) {
    }

    /**
     * 当页面即将构建完成时（异步）
     */
    public void onBuildAsync(@NotNull Inventory inventory) {
    }
}
