package me.skymc.tlm.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;

/**
 * @author sky
 * @since 2018年2月22日 下午3:34:59
 */
public class TLMInventoryHolder implements InventoryHolder {

    private String module;

    private HashMap<String, Object> holderData = new HashMap<>();

    public String getModule() {
        return module;
    }

    public HashMap<String, Object> getHolderData() {
        return holderData;
    }

    /**
     * 构造方法
     *
     * @param module 模块名
     */
    public TLMInventoryHolder(String module) {
        this.module = module;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
