package me.skymc.taboolib.commands.sub.cycle;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import org.bukkit.command.CommandSender;

public class CycleCommand extends SubCommand {

    public CycleCommand(CommandSender sender, String[] args) {
        super(sender, args);
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                new CycleListCommand(sender, args);
            } else if (args[1].equalsIgnoreCase("info")) {
                new CycleInfoCommand(sender, args);
            } else if (args[1].equalsIgnoreCase("reset")) {
                new CycleResetCommand(sender, args);
            } else if (args[1].equalsIgnoreCase("update")) {
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
