package me.skymc.taboolib.commands.sub.sounds;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.commands.sub.sounds.listener.SoundsLibraryPatch;
import me.skymc.taboolib.other.NumberUtils;

/**
 * @author sky
 * @since 2018-03-18 21:02:26
 */
public class SoundsCommand extends SubCommand {
	
	/**
	 * @param sender
	 * @param args
	 */
	public SoundsCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (isPlayer()) {
			if (args.length == 1) {
				SoundsLibraryPatch.openInventory((Player) sender, 1);
			}
			else {
				SoundsLibraryPatch.openInventory((Player) sender, NumberUtils.getInteger(args[1]));
			}
		}
	}
}
