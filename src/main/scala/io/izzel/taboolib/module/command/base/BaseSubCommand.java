package io.izzel.taboolib.module.command.base;

import io.izzel.taboolib.locale.TLocale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public abstract class BaseSubCommand {

    private String label;

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return null;
    }

    public String[] getAliases() {
        return new String[0];
    }

    public CommandArgument[] getArguments() {
        return new CommandArgument[0];
    }

    public CommandType getType() {
        return CommandType.ALL;
    }

    public boolean ignoredLabel() {
        return true;
    }

    public boolean requiredPlayer() {
        return false;
    }

    public String getPermission() {
        return null;
    }

    public boolean hideInHelp() {
        return false;
    }

    public boolean isParameterConform(String[] args) {
        return IntStream.range(0, getArguments().length).noneMatch(i -> getArguments()[i].isRequired() && (args == null || args.length <= i));
    }

    public String getCommandString(String label) {
        return TLocale.asString(getDescription() == null ? "COMMANDS.INTERNAL.COMMAND-HELP-EMPTY" : "COMMANDS.INTERNAL.COMMAND-HELP", label, getLabel(), Arrays.stream(getArguments()).map(parameter -> parameter.toString() + " ").collect(Collectors.joining()), getDescription());
    }

    abstract public void onCommand(CommandSender sender, Command command, String label, String[] args);
}