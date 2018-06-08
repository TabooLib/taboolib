package me.skymc.taboolib.commands.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.javashell.JavaShell;
import org.bukkit.command.CommandSender;

import java.io.File;

public class ShellLoadCommand extends SubCommand {

	public ShellLoadCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (args.length < 2) {
			TLocale.sendTo(sender, "COMMANDS.TABOOLIB.JAVASHELL.INVALID-NAME");
			return;
		}
		
		File file = new File(JavaShell.getScriptFolder(), args[2].contains(".java") ? args[2] : args[2] + ".java");
		if (!file.exists()) {
			TLocale.sendTo(sender, "COMMANDS.TABOOLIB.JAVASHELL.INVALID-SHELL", args[2]);
			return;
		}
		
		if (JavaShell.reloadShell(args[2])) {
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.JAVASHELL.SUCCESS-LOAD", args[2]);
		}
	}

	@Override
	public boolean command() {
		return true;
	}

}
