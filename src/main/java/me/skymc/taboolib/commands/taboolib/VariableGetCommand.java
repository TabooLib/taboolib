package me.skymc.taboolib.commands.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.database.GlobalDataManager;
import org.bukkit.command.CommandSender;

/**
 * @author sky
 */
public class VariableGetCommand extends SubCommand {

    public VariableGetCommand(CommandSender sender, String[] args) {
        super(sender, args);

        if (args.length < 3) {
            TLocale.sendTo(sender, "COAMMNDS.PARAMETER.INSUFFICIENT");
            return;
        }

        if (!("-a".equals(args[1]) || "-s".equals(args[1]))) {
            TLocale.sendTo(sender, "COAMMNDS.TABOOLIB.VARIABLE.READ-ERROR-TYPE");
            return;
        }

        Long time = System.currentTimeMillis();
        String value = null;

        if ("-s".equals(args[1])) {
            value = GlobalDataManager.getVariable(args[2], null);
        } else if ("-a".equals(args[1])) {
            value = GlobalDataManager.getVariableAsynchronous(args[2], null);
        }

        TLocale.sendTo(sender, "COAMMNDS.TABOOLIB.VARIABLE.READ-SUCCESS", String.valueOf(System.currentTimeMillis() - time));
        TLocale.sendTo(sender, "COAMMNDS.TABOOLIB.VARIABLE.READ-RESULT", value == null ? "null" : value);
    }
}
