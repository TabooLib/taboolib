package io.izzel.taboolib.module.command.base.display;

import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.BaseSubCommand;
import org.bukkit.command.CommandSender;

/**
 * @Author sky
 * @Since 2020-04-04 16:14
 */
public abstract class DisplayBase {

    abstract public void displayHead(CommandSender sender, BaseMainCommand baseMainCommand, String label);

    abstract public void displayBottom(CommandSender sender, BaseMainCommand baseMainCommand, String label);

    abstract public void displayParameters(CommandSender sender, BaseSubCommand baseSubCommand, String label);

    abstract public void displayErrorUsage(CommandSender sender, BaseMainCommand baseMainCommand, String label, String help);

    abstract public void displayErrorCommand(CommandSender sender, BaseMainCommand baseMainCommand, String label, String help);

    abstract public String displayHelp(CommandSender sender, BaseSubCommand baseSubCommand, String label);
}
