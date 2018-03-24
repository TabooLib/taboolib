package me.skymc.taboolib.skull;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullUtils {
	
	public static ItemStack getItme(OfflinePlayer p ) {
		SkullMeta s = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		s.setOwner(p.getName());
		
		ItemStack i = new ItemStack(Material.SKULL_ITEM);
		i.setDurability((short) 3);
		i.setItemMeta(s);
		
		return i;
	}
	
	public static ItemStack getOnlineItem(OfflinePlayer p ) {
		if (p.isOnline())
		{
			SkullMeta s = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
			s.setOwner(p.getName());
			
			ItemStack i = new ItemStack(Material.SKULL_ITEM);
			i.setDurability((short) 3);
			i.setItemMeta(s);
			return i;
		}
		return new ItemStack(Material.SKULL_ITEM);
	}

}
