package me.skymc.taboolib.inventory.speciaitem;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.message.MsgUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author sky
 * @since 2018年2月17日 下午8:34:12
 */
public class SpecialItem implements Listener {

    private static SpecialItem specialItem = null;

    private final List<AbstractSpecialItem> ITEM_DATA = new CopyOnWriteArrayList<>();

    private boolean isLoaded;

    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * 构造方法
     */
    private SpecialItem() {

    }

    /**
     * 获取工具对象
     *
     * @return {@link SpecialItem}
     */
    public static SpecialItem getInst() {
        if (specialItem == null) {
            synchronized (SpecialItem.class) {
                if (specialItem == null) {
                    specialItem = new SpecialItem();
                    // 注册监听器
                    Bukkit.getPluginManager().registerEvents(specialItem, Main.getInst());
                }
            }
        }
        return specialItem;
    }

    /**
     * 注册接口
     *
     * @param item 接口对象
     */
    public void register(AbstractSpecialItem item) {
        if (contains(item.getName())) {
            MsgUtils.warn("特殊物品接口已存在, 检查名称 &4" + item.getName() + " &c是否重复");
        } else {
            ITEM_DATA.add(item);
            if (isLoaded) {
                item.onEnable();
            }
        }
    }

    /**
     * 注销接口
     *
     * @param name 注册名称
     */
    public void cancel(String name) {
        for (AbstractSpecialItem specialitem : ITEM_DATA) {
            if (specialitem.getName() != null && specialitem.getName().equals(specialitem.getName())) {
                specialitem.onDisable();
                ITEM_DATA.remove(specialitem);
            }
        }
    }

    /**
     * 注销接口
     *
     * @param plugin 注册插件
     */
    public void cancel(Plugin plugin) {
        for (AbstractSpecialItem specialitem : ITEM_DATA) {
            if (specialitem.getPlugin() != null && specialitem.getPlugin().equals(plugin)) {
                specialitem.onDisable();
                ITEM_DATA.remove(specialitem);
            }
        }
    }

    /**
     * 判断名称是否存在
     *
     * @param name 注册名称
     * @return boolean
     */
    public boolean contains(String name) {
        for (AbstractSpecialItem specialitem : ITEM_DATA) {
            if (specialitem.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 载入所有已注册接口
     */
    public void loadItems() {
        ITEM_DATA.forEach(AbstractSpecialItem::onEnable);
        isLoaded = true;
    }

    /**
     * 注销所有已注册接口
     */
    public void unloadItems() {
        ITEM_DATA.forEach(AbstractSpecialItem::onDisable);
        ITEM_DATA.clear();
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        cancel(e.getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void click(InventoryClickEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (ItemUtils.isNull(e.getCurrentItem()) || ItemUtils.isNull(e.getCursor())) {
            return;
        }
        Player player = (Player) e.getWhoClicked();
        for (AbstractSpecialItem specialItem : ITEM_DATA) {
            for (SpecialItemResult result : specialItem.isCorrectClick(player, e.getCurrentItem(), e.getCursor())) {
                if (result == null) {
                    break;
                }
                switch (result) {
                    case CANCEL:
                        e.setCancelled(true);
                        break;
                    case BREAK:
                        return;
                    case REMOVE_ITEM_CURRENT:
                        e.setCurrentItem(null);
                        break;
                    case REMOVE_ITEM_CURSOR:
                        e.getWhoClicked().setItemOnCursor(null);
                        break;
                    case REMOVE_ITEM_CURRENT_AMOUNT_1:
                        if (e.getCurrentItem().getAmount() > 1) {
                            e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
                        } else {
                            e.setCurrentItem(null);
                        }
                        break;
                    case REMOVE_ITEM_CURSOR_AMOUNT_1:
                        if (e.getCursor().getAmount() > 1) {
                            e.getCursor().setAmount(e.getCursor().getAmount() - 1);
                        } else {
                            e.getWhoClicked().setItemOnCursor(null);
                        }
                        break;
                }
            }
        }
    }
}
