package me.skymc.taboolib.commands.sub;

import com.ilummc.tlib.resources.TLocale;
import org.bukkit.command.CommandSender;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.database.GlobalDataManager;
import me.skymc.taboolib.message.MsgUtils;

public class VariableSetCommand extends SubCommand {

	public VariableSetCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		if (args.length < 4) {
			TLocale.sendTo(sender, "COAMMNDS.PARAMETER.INSUFFICIENT");
			return;
		}

		if (!(args[1].equals("-a") || args[1].equals("-s"))) {
            TLocale.sendTo(sender, "COAMMNDS.TABOOLIB.VARIABLE.WRITE-ERROR-TYPE");
			return;
		}
		
		Long time = System.currentTimeMillis();
		String value = getArgs(3);
		
		if (args[1].equals("-s")) {
			GlobalDataManager.setVariable(args[2], value);
		}
		else if (args[1].equals("-a")) {
			GlobalDataManager.setVariableAsynchronous(args[2], value);
		}

        TLocale.sendTo(sender, "COAMMNDS.TABOOLIB.VARIABLE.WRITE-SUCCESS", String.valueOf(System.currentTimeMillis() - time));
		setReturn(true);
	}
}
