package me.skymc.taboolib.inventory.speciaitem;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * @author sky
 * @since 2018年2月17日 下午8:35:42
 */
public abstract interface AbstractSpecialItem {
	
	/**
	 * 当接口被载入
	 */
	default void onEnable() {}
	
	/**
	 * 当接口被卸载
	 */
	default void onDisable() {}
	
	/**
	 * 获取识别名称
	 * 
	 * @return String
	 */
	abstract String getName();
	
	/**
	 * 获取载入插件
	 * 
	 * @return {@link Plugin}
	 */
	abstract Plugin getPlugin();
	
	/**
	 * 是否进行点击事件
	 * 
	 * @param player 玩家
	 * @param currentItem 点击物品
	 * @param cursorItem 持有物品
	 * @return {@link SpecialItemResult[]}
	 */
	abstract SpecialItemResult[] isCorrectClick(Player player, ItemStack currentItem, ItemStack cursorItem);
}
