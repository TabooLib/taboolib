package me.skymc.taboolib.commands.internal;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public interface InternalSubCommand {

    String getLabel();

    String getDescription();

    InternalArgument[] getArguments();

    void onCommand(CommandSender sender, Command command, String label, String[] args);

}
