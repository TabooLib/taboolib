package me.skymc.tlm.module.sub;

import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.other.DateUtils;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.playerdata.DataUtils;
import me.skymc.tlm.module.ITabooLibraryModule;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sky
 * @since 2018年2月18日 下午12:13:55
 */
public class ModuleKits implements ITabooLibraryModule {
	
	private FileConfiguration data;

	@Override
	public String getName() {
		return "Kits";
	}
	
	@Override
	public void onEnable() {
		data = DataUtils.addPluginData("ModuleKits", null);
	}
	
	/**
	 * 设置玩家是否领取礼包
	 * 
	 * @param player 玩家
	 */
	public void setPlayerReward(Player player, String kit, boolean reward) {
		data.set(kit + "." + player.getName(), reward ? System.currentTimeMillis() : null);
	}
	
	/**
	 * 清空礼包数据
	 * 
	 * @param kit 礼包
	 */
	public void resetKit(String kit) {
		data.set(kit, null);
	}
	
	/**
	 * 玩家是否领取礼包
	 * 
	 * @param player 玩家
	 * @param kit 礼包
	 * @return boolean
	 */
	public boolean isPlayerRewared(Player player, String kit) {
		return data.contains(kit + "." + player.getName());
	}
	
	/**
	 * 礼包是否在冷却中
	 * 
	 * @param player
	 * @param kit
	 * @return
	 */
	public boolean isPlayerCooldown(Player player, String kit) {
		return System.currentTimeMillis() - data.getLong(kit + "." + player.getName()) < getCooldown(kit);
	}
	
	/**
	 * 礼包是否存在
	 * 
	 * @param kit 礼包名
	 * @return boolean
	 */
	public boolean contains(String kit) {
		return getConfig().contains("Kits." + kit);
	}
	
	/**
	 * 获取礼包冷却时间
	 * 
	 * @param kit 礼包名
	 * @return long
	 */
	public long getCooldown(String kit) {
		return DateUtils.formatDate(getConfig().getString("Kits." + kit + ".Cooldown"));
	}
	
	/**
	 * 获取礼包空间不足时的处理方式
	 * 
	 * @param kit 礼包名
	 * @return boolean
	 */
	public Boolean isFullDrop(String kit) {
		return getConfig().getBoolean("Kits." + kit + ".FullDrop");
	}
	
	/**
	 * 礼包是否只能领取一次
	 * 
	 * @param kit 礼包名
	 * @return boolean
	 */
	public boolean isDisposable(String kit) {
		return getConfig().getBoolean("Kits." + kit + ".Disposable");
	}
	
	/**
	 * 获取礼包权限
	 * 
	 * @param kit 礼包名
	 * @return String
	 */
	public String getPermission(String kit) {
		return getConfig().getString("Kits." + kit + ".Permission");
	}
	
	/**
	 * 获取礼包权限提示
	 * 
	 * @param kit 礼包名
	 * @return String
	 */
	public String getPermissionMessage(String kit) {
		return getConfig().getString("Kits." + kit + ".Permission-message").replace("&", "§");
	}
	
	/**
	 * 获取礼包物品
	 * 
	 * @param kit 礼包名
	 * @return {@link List}
	 */
	public List<ItemStack> getItems(String kit) {
		List<ItemStack> items = new ArrayList<>();
		for (String itemStr : getConfig().getStringList("Kits." + kit + ".Items")) {
			ItemStack item = ItemUtils.getCacheItem(itemStr.split(" ")[0]);
			if (item != null) {
				item = item.clone();
				try {
					item.setAmount(NumberUtils.getInteger(itemStr.split(" ")[1]));
					items.add(item);
				} catch (Exception e) {
					MsgUtils.warn("模块配置载入异常: &4物品数量错误");
					MsgUtils.warn("模块: &4Kits");
					MsgUtils.warn("位于: &4" + itemStr);
				}
			}
		}
		return items;
	}
	
	/**
	 * 获取礼包命令
	 * 
	 * @param kit 礼包名
	 * @return {@link List}
	 */
	public List<String> getCommands(String kit) {
		return getConfig().contains("Kits." + kit + ".Commands") ? getConfig().getStringList("Kits." + kit + ".Commands") : new ArrayList<>();
	}
}
