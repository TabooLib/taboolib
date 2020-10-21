package io.izzel.taboolib.module.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public enum CommandType {
	
	CONSOLE, PLAYER, ALL;

	public boolean isType(CommandSender sender) {
		switch (this) {
			case CONSOLE:
				return sender instanceof ConsoleCommandSender;
			case PLAYER:
				return sender instanceof Player;
			default:
				return true;
		}
	}
}
