package me.skymc.taboolib.commands.builder.type;

import org.bukkit.command.CommandSender;

/**
 * 为了防止与 CommandExecutor 混淆所以名称改为 CompleterCommand
 *
 * @author sky
 */
public interface CompleterCommand {

    boolean execute(CommandSender sender, String[] args);

}