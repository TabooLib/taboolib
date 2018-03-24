package me.skymc.taboolib.commands.sub.shell;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.message.MsgUtils;
import org.bukkit.command.CommandSender;

public class ShellCommand extends SubCommand {

	public ShellCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("load")) {
				new ShellLoadCommand(sender, args);
			}
			else if (args[1].equalsIgnoreCase("unload")) {
				new ShellUnloadCommand(sender, args);
			}
		}
		else {
			MsgUtils.send(sender, "&4指令错误");
		}
	}

	@Override
	public boolean command() {
		return true;
	}

}
