package io.izzel.taboolib.module.command.base;

import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.util.Strings;
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
    private SubCommand annotation;

    public void setAnnotation(SubCommand annotation) {
        this.annotation = annotation;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return annotation.description();
    }

    public String[] getAliases() {
        return annotation.aliases();
    }

    public Argument[] getArguments() {
        return Arrays.stream(annotation.arguments()).map(a -> a.endsWith("?") ? new Argument(a.substring(0, a.length() - 1), false) : new Argument(a)).toArray(Argument[]::new);
    }

    public CommandType getType() {
        return annotation.type();
    }

    public boolean ignoredLabel() {
        return annotation.ignoredLabel();
    }

    public boolean requiredPlayer() {
        return annotation.requiredPlayer();
    }

    public String getPermission() {
        return annotation.permission();
    }

    public boolean hideInHelp() {
        return annotation.hideInHelp();
    }

    public boolean isParameterConform(String[] args) {
        return IntStream.range(0, getArguments().length).noneMatch(i -> getArguments()[i].isRequired() && (args == null || args.length <= i));
    }

    public String getCommandString(String label) {
        return TLocale.asString(Strings.isEmpty(getDescription()) ? "COMMANDS.INTERNAL.COMMAND-HELP-EMPTY" : "COMMANDS.INTERNAL.COMMAND-HELP", label, getLabel(), Arrays.stream(getArguments()).map(parameter -> parameter.toString() + " ").collect(Collectors.joining()), getDescription());
    }

    abstract public void onCommand(CommandSender sender, Command command, String label, String[] args);
}