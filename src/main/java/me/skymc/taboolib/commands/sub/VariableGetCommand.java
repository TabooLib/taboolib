package me.skymc.taboolib.commands.sub;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.database.GlobalDataManager;
import org.bukkit.command.CommandSender;

public class VariableGetCommand extends SubCommand {

	public VariableGetCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		if (args.length < 3) {
            TLocale.sendTo(sender, "COAMMNDS.PARAMETER.INSUFFICIENT");
            return;
        }

        if (!(args[1].equals("-a") || args[1].equals("-s"))) {
            TLocale.sendTo(sender, "COAMMNDS.TABOOLIB.VARIABLE.READ-ERROR-TYPE");
            return;
		}
		
		Long time = System.currentTimeMillis();
		String value = null;
		
		if (args[1].equals("-s")) {
			value = GlobalDataManager.getVariable(args[2], null);
		}
		else if (args[1].equals("-a")) {
			value = GlobalDataManager.getVariableAsynchronous(args[2], null);
		}

        TLocale.sendTo(sender, "COAMMNDS.TABOOLIB.VARIABLE.READ-SUCCESS", String.valueOf(System.currentTimeMillis() - time));
        TLocale.sendTo(sender, "COAMMNDS.TABOOLIB.VARIABLE.READ-RESULT", value == null ? "null" : value);
	}
}
