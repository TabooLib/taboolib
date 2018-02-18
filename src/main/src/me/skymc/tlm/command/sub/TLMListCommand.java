package me.skymc.tlm.command.sub;

import org.bukkit.command.CommandSender;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.tlm.module.ITabooLibraryModule;
import me.skymc.tlm.module.TabooLibraryModule;

/**
 * @author sky
 * @since 2018年2月18日 下午2:10:12
 */
public class TLMListCommand extends SubCommand {

	/**
	 * @param sender
	 * @param args
	 */
	public TLMListCommand(CommandSender sender, String[] args) {
		super(sender, args);
		sender.sendMessage("§f");
		sender.sendMessage("§b§l----- §3§lTaooLibraryModule Modules §b§l-----");
		sender.sendMessage("§f");
		
		for (ITabooLibraryModule module : TabooLibraryModule.getInst().keySet()) {
			sender.sendMessage("§f - §8" + module.getName());
		}
		
		sender.sendMessage("§f");
	}

}
