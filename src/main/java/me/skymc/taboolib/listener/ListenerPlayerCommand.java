package me.skymc.taboolib.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.database.PlayerDataManager;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.itemnbtapi.NBTCompound;
import me.skymc.taboolib.itemnbtapi.NBTItem;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.RunCommandEvent;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.permission.PermissionUtils;
import me.skymc.taboolib.playerdata.DataUtils;

public class ListenerPlayerCommand implements Listener {
	
	@EventHandler
	public void cmd(ServerCommandEvent e) {
		if (e.getCommand().equals("savefile")) {
			if (TabooLib.getVerint() > 10700) {
				e.setCancelled(true);
			}
			Bukkit.getScheduler().runTask(Main.getInst(), () -> DataUtils.saveAllCaches());
			Bukkit.getScheduler().runTask(Main.getInst(), () -> PlayerDataManager.saveAllCaches(true, false));
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void cmd(PlayerCommandPreprocessEvent e) {
		if (e.getMessage().equals("/unbreakable") && PermissionUtils.hasPermission(e.getPlayer(), "taboolib.unbreakable")) {
			e.setCancelled(true);
			
			NBTItem nbti = new NBTItem(e.getPlayer().getItemInHand());
			nbti.setInteger("Unbreakable", 1);
			e.getPlayer().setItemInHand(nbti.getItem());
			
			MsgUtils.send(e.getPlayer(), "Success!");
		}
	}
}
