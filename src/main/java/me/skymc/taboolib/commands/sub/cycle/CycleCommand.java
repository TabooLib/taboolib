package me.skymc.taboolib.commands.sub.cycle;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import org.bukkit.command.CommandSender;

public class CycleCommand extends SubCommand {

    public CycleCommand(CommandSender sender, String[] args) {
        super(sender, args);
        if (args.length > 1) {
            if ("list".equalsIgnoreCase(args[1])) {
                new CycleListCommand(sender, args);
            } else if ("info".equalsIgnoreCase(args[1])) {
                new CycleInfoCommand(sender, args);
            } else if ("reset".equalsIgnoreCase(args[1])) {
                new CycleResetCommand(sender, args);
            } else if ("update".equalsIgnoreCase(args[1])) {
                new CycleUpdateCommand(sender, args);
            }
        } else {
            TLocale.sendTo(sender, "COMMANDS.PARAMETER.UNKNOWN");
        }
    }

    @Override
    public boolean command() {
        return true;
    }

}
