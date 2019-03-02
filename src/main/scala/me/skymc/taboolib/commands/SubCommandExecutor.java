package me.skymc.taboolib.commands;

import org.bukkit.command.CommandSender;

@Deprecated
public interface SubCommandExecutor {

    boolean command(CommandSender sender, String[] args);

}
