package me.skymc.taboolib.commands.sub.shell;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import org.bukkit.command.CommandSender;

public class ShellCommand extends SubCommand {

    public ShellCommand(CommandSender sender, String[] args) {
        super(sender, args);
        if (args.length > 1) {
            if ("load".equalsIgnoreCase(args[1])) {
                new ShellLoadCommand(sender, args);
            } else if ("unload".equalsIgnoreCase(args[1])) {
                new ShellUnloadCommand(sender, args);
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
