package me.skymc.tlm.command.sub;


import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.tlm.TLM;
import me.skymc.tlm.inventory.TLMInventoryHolder;
import me.skymc.tlm.module.TabooLibraryModule;
import me.skymc.tlm.module.sub.ModuleInventorySave;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author sky
 * @since 2018年2月18日 下午2:53:58
 */
public class TLMInvCommand extends SubCommand {

	/**
	 * @param sender
	 * @param args
	 */
	public TLMInvCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (TabooLibraryModule.getInst().valueOf("InventorySave") == null) {
			TLM.getInst().getLanguage().get("INV-DISABLE").send(sender);
			return;
		}
		
		// 获取模块
		ModuleInventorySave moduleInventorySave = (ModuleInventorySave) TabooLibraryModule.getInst().valueOf("InventorySave");
		
		// 判断命令
		if (args.length == 1) {
			TLM.getInst().getLanguage().get("INV-EMPTY").send(sender);
		}
		
		// 列出背包
		else if ("list".equalsIgnoreCase(args[1])) {
			TLM.getInst().getLanguage().get("INV-LIST").addPlaceholder("$name", moduleInventorySave.getInventorys().toString()).send(sender);
		}
		
		// 查看背包
		else if ("info".equalsIgnoreCase(args[1])) {
			// 如果是后台
			if (!(sender instanceof Player)) {
				TLM.getInst().getLanguage().get("INV-CONSOLE").send(sender);
				return;
			}
			
			// 判断长度
			if (args.length < 3) {
				TLM.getInst().getLanguage().get("INV-NAME").send(sender);
				return;
			}
			
			// 判断背包
			if (!moduleInventorySave.getInventorys().contains(args[2])) {
				TLM.getInst().getLanguage().get("INV-NOTFOUND").addPlaceholder("$name", args[2]).send(sender);
				return;
			}
			
			// 获取玩家
			Player player = (Player) sender;
			
			// 获取物品
			List<ItemStack> items = moduleInventorySave.getItems(args[2]);
			
			// 打开界面
			Inventory inv = Bukkit.createInventory(new TLMInventoryHolder("InventorySave"), 54, TLM.getInst().getLanguage().get("INV-INFO-TITLE")
					.addPlaceholder("$name", args[2])
					.asString());
			
			// 设置物品
			ItemStack barrier = ItemUtils.setName(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), "§f"); 
			
			for (int i = 9 ; i < 18 ; i++) {
				inv.setItem(i, barrier);
			}
			
			for (int i = 9 ; i < 35 ; i++) {
				inv.setItem(i + 9, items.get(i));
			}
			
			for (int i = 0 ; i < 9 ; i++) {
				inv.setItem(i + 45, items.get(i));
			}
			
			inv.setItem(1, items.get(39));
			inv.setItem(2, items.get(38));
			inv.setItem(3, items.get(37));
			inv.setItem(4, items.get(36));
			
			// 判断版本
			if (items.size() == 41) {
				inv.setItem(6, items.get(40));
			}
			
			// 打开背包
			player.openInventory(inv);
		}
		
		// 保存背包
		else if ("save".equalsIgnoreCase(args[1])) {
			// 如果是后台
			if (!(sender instanceof Player)) {
				TLM.getInst().getLanguage().get("INV-CONSOLE").send(sender);
				return;
			}
			
			// 判断长度
			if (args.length < 3) {
				TLM.getInst().getLanguage().get("INV-NAME").send(sender);
				return;
			}
			
			// 获取玩家
			Player player = (Player) sender;
			
			// 保存背包
			moduleInventorySave.saveInventory(player, args[2]);
			
			// 提示信息
			TLM.getInst().getLanguage().get("INV-SAVE").addPlaceholder("$name", args[2]).send(player);
		}
		
		// 覆盖背包
		else if ("paste".equalsIgnoreCase(args[1])) {
			// 判断长度
			if (args.length < 3) {
				TLM.getInst().getLanguage().get("INV-NAME").send(sender);
				return;
			}
			
			// 判断背包
			if (!moduleInventorySave.getInventorys().contains(args[2])) {
				TLM.getInst().getLanguage().get("INV-NOTFOUND").addPlaceholder("$name", args[2]).send(sender);
				return;
			}
			
			// 获取玩家
			Player player;
			if (args.length > 3) {
				player = Bukkit.getPlayerExact(args[3]);
				// 玩家不存在
				if (player == null) {
					TLM.getInst().getLanguage().get("INV-OFFLINE").addPlaceholder("$name", args[3]).send(sender);
					return;
				}
			} else if (sender instanceof Player) {
				player = (Player) sender;
			} else {
				TLM.getInst().getLanguage().get("INV-CONSOLE").send(sender);
				return;
			}
			
			// 覆盖背包
			moduleInventorySave.pasteInventory(player, args[2], args.length > 4 ? args[3] : "null");
			
			// 如果是玩家
			if (sender instanceof Player) {
				// 提示信息
				TLM.getInst().getLanguage().get("INV-PASTE")
					.addPlaceholder("$name", args[2])
					.addPlaceholder("$player", player.getName())
					.send(player);
			}
		}
		
		// 删除背包
		else if ("delete".equalsIgnoreCase(args[1])) {
			// 判断长度
			if (args.length < 3) {
				TLM.getInst().getLanguage().get("INV-NAME").send(sender);
				return;
			}
			
			// 判断背包
			if (!moduleInventorySave.getInventorys().contains(args[2])) {
				TLM.getInst().getLanguage().get("INV-NOTFOUND").addPlaceholder("$name", args[2]).send(sender);
				return;
			}
			
			// 删除
			moduleInventorySave.deleteInventory(args[2]);
			
			// 提示信息
			TLM.getInst().getLanguage().get("KIT-DELETE").addPlaceholder("$name", args[2]).send(sender);
		}
		
		else {
			TLM.getInst().getLanguage().get("INV-EMPTY").send(sender);
		}
	}
}
