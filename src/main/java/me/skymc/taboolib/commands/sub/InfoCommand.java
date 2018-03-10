package me.skymc.taboolib.commands.sub;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.itemnbtapi.NBTItem;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowItemEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;
import me.skymc.taboolib.message.MsgUtils;

public class InfoCommand extends SubCommand {

	@SuppressWarnings("deprecation")
	public InfoCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		if (isPlayer()) {
			Player player = (Player) sender;
			if (player.getItemInHand().getType().equals(Material.AIR)) {
				MsgUtils.send(player, "&7请手持正确物品");
			}
			else {
				sender.sendMessage("§f");
				sender.sendMessage("§b§l----- §3§lItemStack Info §b§l-----");
				sender.sendMessage("§f");
				
				JSONFormatter json = new JSONFormatter();
				json.append("§7 - 物品材质: §f"); json.appendHoverClick("§f" + player.getItemInHand().getType().name(), new ShowTextEvent("§f点击复制"), new SuggestCommandEvent(player.getItemInHand().getType().name()));
				json.newLine();
				json.append("§7 - 物品名称: §f"); json.appendHoverClick("§f" + ItemUtils.getCustomName(player.getItemInHand()), new ShowTextEvent("§f点击复制"), new SuggestCommandEvent(ItemUtils.getCustomName(player.getItemInHand()).replace("§", "&")));
				json.newLine();
				json.append("§7 - 物品序号: §f" + player.getItemInHand().getTypeId() + ":" + player.getItemInHand().getDurability());
				json.newLine();
				json.append("§7 - 物品展示: §f"); json.appendHover(ItemUtils.getCustomName(player.getItemInHand()), new ShowItemEvent(player.getItemInHand()));
				json.send(player);
				
				NBTItem nbt = new NBTItem(((Player) sender).getItemInHand());
				sender.sendMessage("§7 - 物品 NBT: §f");
				sender.sendMessage("§f");
				sender.sendMessage(nbt.toString());
				
				sender.sendMessage("§f");
			}
		}
	}
}
