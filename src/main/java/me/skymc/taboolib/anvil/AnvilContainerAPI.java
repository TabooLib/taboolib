package me.skymc.taboolib.anvil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.skymc.taboolib.anvil.versions.AnvilContainer_V1_9_4;
import me.skymc.taboolib.methods.MethodsUtils;

public class AnvilContainerAPI implements Listener{
	
	public static List<String> list = new ArrayList<>();
	public static ItemStack item = new ItemStack(Material.NAME_TAG);
	
	public static HashMap<String, String> isOpen = new HashMap<>();
	public static AnvilContainerAPIEvent event;
	
	public static void send(Player p, String type, String str, List<String> lorelist)
	{
		isOpen.put(p.getName(), type);
		
		AnvilContainer_V1_9_4.openAnvil(p);
		ItemMeta meta = item.getItemMeta();
		
		list.clear();
		if (lorelist == null)
		{
			list.add("");
			list.add("§7在上方文本框内输入信息");
			list.add("§7随后点击右侧输出物品");
		}
		else
		{
			list = lorelist;
		}
		meta.setLore(list);
		meta.setDisplayName(str);
		item.setItemMeta(meta);
		
		p.getOpenInventory().setItem(0, item);
		p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
	}
	
	@EventHandler
	public void close(InventoryCloseEvent e)
	{
		if (isOpen.containsKey(e.getPlayer().getName()))
		{
			isOpen.remove(e.getPlayer().getName());
			if (e.getInventory().getType() == InventoryType.ANVIL)
			{
				e.getInventory().clear();
			}
		}
	}
	
	@EventHandler
	public void click(InventoryClickEvent e)
	{
		if (!isOpen.containsKey(e.getWhoClicked().getName()))
		{
			return;
		}
		if (e.getInventory().getType() != InventoryType.ANVIL)
		{
			return;
		}
		e.setCancelled(true);
		
		int slot = e.getRawSlot();
		if (slot != 2)
		{
			return;
		}
		
		Inventory inv = e.getInventory();
		if (inv.getItem(2) == null)
		{
			return;
		}
		
		if (inv.getItem(2).getItemMeta().hasDisplayName())
		{
			event = new AnvilContainerAPIEvent(e, isOpen.get(e.getWhoClicked().getName()), inv.getItem(2).getItemMeta().getDisplayName());
			e.getWhoClicked().closeInventory();
			Bukkit.getPluginManager().callEvent(event);
		}
	}
	
	@EventHandler
	public void example(PlayerCommandPreprocessEvent e)
	{
		if (e.getMessage().equals("/anvilexample"))
		{
			if (e.getPlayer().hasPermission("taboolib.admin"))
			{
				e.setCancelled(true);
				AnvilContainerAPI.send(e.getPlayer(), "EXAMPLE", "在这里输入文本", null);
			}
		}
	}
	
	@EventHandler
	public void example2(AnvilContainerAPIEvent e)
	{
		if (e.type.equals("EXAMPLE"))
		{
			e.event.getWhoClicked().sendMessage(e.string);
		}
	}
}
