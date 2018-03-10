package me.skymc.taboolib.commands;

import org.bukkit.command.CommandSender;

@Deprecated
public abstract interface SubCommandExecutor {
	
	public abstract boolean command(CommandSender sender, String[] args);

}
