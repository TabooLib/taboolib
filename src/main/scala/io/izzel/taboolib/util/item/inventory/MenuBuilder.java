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

import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2019-05-21 15:54
 */
public class MenuBuilder {

    private Map<Character, ItemStack> keys = Maps.newHashMap();
    private Plugin plugin;
    private String title;
    private int rows;
    private char[][] items = new char[0][0];
    private ClickTask clickTask;
    private CloseTask closeTask;
    private BuildTask buildTask;
    private BuildTask buildTaskAsync;
    private boolean lockHand;

    public MenuBuilder(Plugin plugin) {
        this.plugin = plugin;
    }

    public static MenuBuilder builder(Plugin plugin) {
        return new MenuBuilder(plugin);
    }

    public static MenuBuilder builder() {
        return new MenuBuilder(Ref.getCallerPlugin(Ref.getCallerClass(3).orElse(TabooLib.class)));
    }

    public MenuBuilder lockHand() {
        this.lockHand = true;
        return this;
    }

    public MenuBuilder lockHand(boolean value) {
        this.lockHand = value;
        return this;
    }

    public MenuBuilder event(ClickTask clickTask) {
        this.clickTask = clickTask;
        return this;
    }

    public MenuBuilder close(CloseTask closeTask) {
        this.closeTask = closeTask;
        return this;
    }

    public MenuBuilder build(BuildTask buildTask) {
        this.buildTask = buildTask;
        return this;
    }

    public MenuBuilder buildAsync(BuildTask buildTask) {
        this.buildTaskAsync = buildTask;
        return this;
    }

    public MenuBuilder title(String title) {
        this.title = title;
        return this;
    }

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

    public void open(Player player) {
        player.openInventory(build());
    }

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

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

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
