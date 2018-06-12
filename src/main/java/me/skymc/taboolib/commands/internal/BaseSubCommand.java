package me.skymc.taboolib.commands.internal;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.internal.type.CommandType;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public abstract class BaseSubCommand implements ISubCommand {

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

    public boolean isParameterConform(String[] args) {
        return IntStream.range(0, getArguments().length).noneMatch(i -> getArguments()[i].isRequired() && (args == null || args.length <= i));
    }

    public String getCommandString(String label) {
        String stringBuilder = Arrays.stream(getArguments()).map(parameter -> parameter.toString() + " ").collect(Collectors.joining());
        return TLocale.asString("COMMANDS.INTERNAL.COMMAND-HELP", label, getLabel(), stringBuilder.trim(), getDescription());
    }
}