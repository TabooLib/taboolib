package me.skymc.tlm.module.sub;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.playerdata.DataUtils;
import me.skymc.tlm.TLM;
import me.skymc.tlm.annotation.DisableConfig;
import me.skymc.tlm.inventory.TLMInventoryHolder;
import me.skymc.tlm.module.ITabooLibraryModule;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author sky
 * @since 2018年2月22日 下午2:48:27
 */
@DisableConfig
public class ModuleInventorySave implements ITabooLibraryModule, Listener {

    private FileConfiguration conf;

    @Override
    public String getName() {
        return "InventorySave";
    }

    @Override
    public void onEnable() {
        reloadConfig();
    }

    @Override
    public void onReload() {
        reloadConfig();
    }

    public void reloadConfig() {
        conf = DataUtils.addPluginData("InventorySave", Main.getInst());
    }

    /**
     * 保存玩家背包
     *
     * @param player 玩家
     * @param name   名称
     */
    public void saveInventory(Player player, String name) {
        // 设置物品
        for (int i = 0; i < (TabooLib.getVerint() > 10800 ? 41 : 40); i++) {
            ItemStack item = player.getInventory().getItem(i);
            conf.set(name + "." + i, item == null ? new ItemStack(Material.AIR) : item.clone());
        }
    }

    /**
     * 覆盖玩家背包
     *
     * @param player 玩家
     * @param name   名称
     */
    public void pasteInventory(Player player, String name) {
        pasteInventory(player, name, "null");
    }

    /**
     * 覆盖玩家背包
     *
     * @param player 玩家
     * @param name   名称
     * @param module 模式
     */
    public void pasteInventory(Player player, String name, String module) {
        // 如果背包不存在
        if (!conf.contains(name)) {
            TLM.getInst().runtimeFall("InventorySave", "InventoryNotFound", name);
            return;
        }
        // 异常物品
        List<ItemStack> otherItem = new ArrayList<>();
        // 设置物品
        for (int i = 0; i < (TabooLib.getVerint() > 10800 ? 41 : 40); i++) {
            try {
                ItemStack item = (ItemStack) conf.get(name + "." + i);
                // 如果原本有物品
                if (!ItemUtils.isNull(player.getInventory().getItem(i))) {
                    // 跳过
                    if ("-b".equalsIgnoreCase(module)) {
                        continue;
                    }
                    // 给予
                    else if ("-a".equalsIgnoreCase(module)) {
                        otherItem.add(item);
                        continue;
                    }
                }
                // 覆盖
                player.getInventory().setItem(i, item);
            } catch (Exception e) {
                TLM.getInst().runtimeFall("InventorySave", "InventoryCoverFall", name);
            }
        }
        // 循环异常物品
        for (ItemStack item : otherItem) {
            player.getInventory().addItem(item);
        }
    }

    /**
     * 获取背包内所有物品
     *
     * @param name 背包名称
     * @return {@link List}
     */
    public List<ItemStack> getItems(String name) {
        // 如果背包不存在
        if (!conf.contains(name)) {
            TLM.getInst().runtimeFall("InventorySave", "InventoryNotFound", name);
            return new LinkedList<>();
        }

        List<ItemStack> items = new LinkedList<>();
        // 设置物品
        for (int i = 0; i < (TabooLib.getVerint() > 10800 ? 41 : 40); i++) {
            try {
                ItemStack item = (ItemStack) conf.get(name + "." + i);
                items.add(item);
            } catch (Exception e) {
                TLM.getInst().runtimeFall("InventorySave", "ItemStackLoadFall", name);
            }
        }
        return items;
    }

    /**
     * 获取所有背包
     *
     * @return {@link Set}
     */
    public Set<String> getInventorys() {
        return conf.getConfigurationSection("").getKeys(false);
    }

    /**
     * 删除背包
     *
     * @param name 名称
     */
    public void deleteInventory(String name) {
        conf.set(name, null);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof TLMInventoryHolder)) {
            return;
        }

        TLMInventoryHolder holder = (TLMInventoryHolder) e.getInventory().getHolder();
        if (holder.getModule().equals(getName())) {
            e.setCancelled(true);
        }
    }
}
