package me.skymc.taboolib.commands.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.database.GlobalDataManager;
import org.bukkit.command.CommandSender;

public class VariableSetCommand extends SubCommand {

    public VariableSetCommand(CommandSender sender, String[] args) {
        super(sender, args);

        if (args.length < 4) {
            TLocale.sendTo(sender, "COMMANDS.PARAMETER.INSUFFICIENT");
            return;
        }

        if (!("-a".equals(args[1]) || "-s".equals(args[1]))) {
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.VARIABLE.WRITE-ERROR-TYPE");
            return;
        }

        Long time = System.currentTimeMillis();
        String value = getArgs(3);

        if ("-s".equals(args[1])) {
            GlobalDataManager.setVariable(args[2], value);
        } else if ("-a".equals(args[1])) {
            GlobalDataManager.setVariableAsynchronous(args[2], value);
        }

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.VARIABLE.WRITE-SUCCESS", String.valueOf(System.currentTimeMillis() - time));
        setReturn(true);
    }
}
