package me.skymc.taboolib.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Deprecated
public abstract class SubCommand {

    public CommandSender sender;
    public String[] args;

    public boolean returnValue = false;

    public SubCommand(CommandSender sender, String[] args) {
        this.sender = sender;
        this.args = args;
    }

    public boolean setReturn(boolean returnValue) {
        return this.returnValue = returnValue;
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public boolean command() {
        return returnValue;
    }

    public String getArgs(int size) {
        return IntStream.range(size, args.length).mapToObj(i -> args[i] + " ").collect(Collectors.joining()).trim();
    }
}
