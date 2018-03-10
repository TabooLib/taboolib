package me.skymc.taboolib.inventory.speciaitem;

/**
 * @author sky
 * @since 2018年2月17日 下午8:55:36
 */
public enum SpecialItemResult {
	
	/**
	 * 停止接口检测
	 */
	BREAK,
	
	/**
	 * 取消点击事件
	 */
	CANCEL,
	
	/**
	 * 移除点击物品
	 */
	REMOVE_ITEM_CURRENT,
	
	/**
	 * 移除鼠标物品
	 */
	REMOVE_ITEM_CURSOR,
	
	/**
	 * 移除一个点击物品
	 */
	REMOVE_ITEM_CURRENT_AMOUNT_1,
	
	/**
	 * 移除一个鼠标物品
	 */
	REMOVE_ITEM_CURSOR_AMOUNT_1;
	
}
