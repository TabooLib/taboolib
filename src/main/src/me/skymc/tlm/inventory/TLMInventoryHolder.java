package me.skymc.tlm.inventory;

import java.util.HashMap;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import lombok.Getter;

/**
 * @author sky
 * @since 2018年2月22日 下午3:34:59
 */
public class TLMInventoryHolder implements InventoryHolder {
	
	@Getter
	private String module;
	
	@Getter
	private HashMap<String, Object> holderData = new HashMap<>();
	
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
