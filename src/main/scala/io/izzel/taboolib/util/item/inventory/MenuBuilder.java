package io.izzel.taboolib.util.item.inventory;

import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.util.Ref;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 界面构建工具
 *
 * @Author 坏黑
 * @Since 2019-05-21 15:54
 */
public class MenuBuilder {

    private final Map<Character, ItemStack> keys = Maps.newHashMap();
    private final Plugin plugin;
    private String title;
    private int rows;
    private char[][] items = new char[0][0];
    private ClickTask clickTask = r -> {
    };
    private CloseTask closeTask = r -> {
    };
    private BuildTask buildTask = r -> {
    };
    private BuildTask buildTaskAsync = r -> {
    };
    private boolean lockHand;

    public MenuBuilder(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 获取一个 MenuBuilder 实例
     *
     * @param plugin 插件主类实例
     * @return MenuBuilder 实例
     */
    public static MenuBuilder builder(Plugin plugin) {
        return new MenuBuilder(plugin);
    }

    /**
     * 获取一个 MenuBuilder 实例
     *
     * @return MenuBuilder 实例
     */
    public static MenuBuilder builder() {
        return new MenuBuilder(Ref.getCallerPlugin(Ref.getCallerClass(3).orElse(TabooLib.class)));
    }

    /**
     * 为玩家打开已由 MenuBuilder 创建好的菜单
     *
     * @param player 需要打开菜单的玩家
     */
    public void open(@NotNull Player player) {
        player.openInventory(build());
    }

    /**
     * 将菜单设置为锁定玩家手部动作
     * 这将阻止玩家在使用菜单时进行包括但不限于
     * 丢弃物品，拿出菜单物品
     * 等行为
     * <p>
     * 主要用于处理玩家在打开菜单之前移动手持物品，导致插件产生逻辑错误。
     *
     * @return 编辑过的 MenuBuilder 实例
     */
    public MenuBuilder lockHand() {
        this.lockHand = true;
        return this;
    }

    /**
     * 设置是否锁定玩家手部动作
     * 设置为 true 则将阻止玩家在使用菜单时进行包括但不限于
     * 丢弃物品，拿出菜单物品
     * 等行为
     *
     * @return 编辑过的 MenuBuilder 实例
     */
    public MenuBuilder lockHand(boolean value) {
        this.lockHand = value;
        return this;
    }

    /**
     * 设置玩家点击菜单事件
     *
     * @param clickTask 玩家点击菜单任务
     * @return 编辑过的 MenuBuilder 实例
     */
    public MenuBuilder event(@NotNull ClickTask clickTask) {
        this.clickTask = clickTask;
        return this;
    }

    /**
     * 设置玩家关闭菜单事件
     *
     * @param closeTask 玩家关闭菜单任务
     * @return 编辑过的 MenuBuilder 实例
     */
    public MenuBuilder close(@NotNull CloseTask closeTask) {
        this.closeTask = closeTask;
        return this;
    }

    /**
     * 构建菜单内容
     *
     * @param buildTask 菜单构建任务
     * @return 编辑过的 MenuBuilder 实例
     */
    public MenuBuilder build(@NotNull BuildTask buildTask) {
        this.buildTask = buildTask;
        return this;
    }

    /**
     * 异步构建菜单内容
     *
     * @param buildTask 菜单构建任务
     * @return 编辑过的 MenuBuilder 实例
     */
    public MenuBuilder buildAsync(@NotNull BuildTask buildTask) {
        this.buildTaskAsync = buildTask;
        return this;
    }

    /**
     * 设置菜单标题
     *
     * @param title 标题
     * @return 编辑过的 MenuBuilder 实例
     */
    public MenuBuilder title(@NotNull String title) {
        this.title = title;
        return this;
    }

    /**
     * 设置菜单行数
     *
     * @param rows 菜单行数
     * @return 编辑过的 MenuBuilder 实例
     */
    public MenuBuilder rows(int rows) {
        this.rows = rows * 9;
        return this;
    }

    public MenuBuilder put(char key, ItemStack item) {
        keys.put(key, item);
        return this;
    }

    public MenuBuilder items(String... placeholder) {
        items = new char[placeholder.length][];
        for (int i = 0; i < placeholder.length; i++) {
            items[i] = placeholder[i].toCharArray();
        }
        return this;
    }

    /**
     * 构建菜单
     *
     * @return Bukkit 的 Inventory 背包对象实例
     */
    public Inventory build() {
        Inventory inventory = Bukkit.createInventory(new MenuHolder(this), rows, String.valueOf(title));
        for (int i = 0; i < items.length && i < rows; i++) {
            char[] line = items[i];
            for (int j = 0; j < line.length && j < 9; j++) {
                inventory.setItem(i * 9 + j, keys.getOrDefault(line[j], new ItemStack(Material.AIR)));
            }
        }
        return inventory;
    }

    public char getSlot(int slot) {
        for (int i = 0; i < items.length && i < rows; i++) {
            char[] line = items[i];
            for (int j = 0; j < line.length && j < 9; j++) {
                if (i * 9 + j == slot) {
                    return line[j];
                }
            }
        }
        return ' ';
    }

    public Map<Character, ItemStack> getKeys() {
        return keys;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public char[][] getItems() {
        return items;
    }

    public ClickTask getClickTask() {
        return clickTask;
    }

    public CloseTask getCloseTask() {
        return closeTask;
    }

    public BuildTask getBuildTask() {
        return buildTask;
    }

    public BuildTask getBuildTaskAsync() {
        return buildTaskAsync;
    }

    public boolean isLockHand() {
        return lockHand;
    }
}
