package me.skymc.taboolib.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class SubCommand {
	
	public CommandSender sender;
	public String[] args;
	
	public boolean returnValue = false;
	
	public SubCommand(CommandSender sender, String[] args) {
		this.sender = sender;
		this.args = args;
	}
	
	public boolean setReturn(boolean returnValue) {
		return this.returnValue = returnValue; 
	}
	
	public boolean isPlayer() {
		return sender instanceof Player;
	}
	
	public boolean command() {
		return returnValue;
	}
	
	public String getArgs(int size) {
		StringBuffer sb = new StringBuffer();
		for (int i = size ; i < args.length ; i++) {
			sb.append(args[i]);
			sb.append(" ");
		}
		return sb.toString().substring(0, sb.length() - 1);
	}
}
