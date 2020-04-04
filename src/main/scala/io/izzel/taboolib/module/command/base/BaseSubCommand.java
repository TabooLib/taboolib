package io.izzel.taboolib.module.command.base;

import io.izzel.taboolib.util.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public abstract class BaseSubCommand {

    private String label;
    private boolean player;
    private SubCommand annotation;
    private BaseMainCommand mainCommand;

    abstract public void onCommand(CommandSender sender, Command command, String label, String[] args);

    public BaseMainCommand getMainCommand() {
        return mainCommand;
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
        return player ? CommandType.PLAYER : annotation.type();
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

    public String getCommandString(CommandSender sender, String label) {
        return mainCommand.getDisplay().displayHelp(sender, this, label);
    }

    public boolean hasPermission(CommandSender sender) {
        return Strings.isBlank(getPermission()) || sender.hasPermission(getPermission());
    }

    protected BaseSubCommand label(String label) {
        this.label = label;
        return this;
    }

    protected BaseSubCommand player() {
        player = true;
        return this;
    }

    protected BaseSubCommand annotation(SubCommand annotation) {
        this.annotation = annotation;
        return this;
    }

    protected BaseSubCommand mainCommand(BaseMainCommand mainCommand) {
        this.mainCommand = mainCommand;
        return this;
    }
}