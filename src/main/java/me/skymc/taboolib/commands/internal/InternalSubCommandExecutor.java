package me.skymc.taboolib.commands.internal;

import java.util.stream.IntStream;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public abstract class InternalSubCommandExecutor implements InternalSubCommand {

    public InternalSubCommandType getType() {
        return InternalSubCommandType.ALL;
    }

    public boolean requiredPlayer() {
        return false;
    }

    public boolean isParameterConform(String[] args) {
        return IntStream.range(0, getArguments().length).noneMatch(i -> getArguments()[i].isRequired() && (args == null || args.length <= i));
    }

    public String getCommandString(String label) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" §f/");
        stringBuilder.append(label);
        stringBuilder.append(" ");
        stringBuilder.append(getLabel());
        stringBuilder.append(" ");
        for (InternalArgument parameter : getArguments()) {
            stringBuilder.append(parameter.toString());
            stringBuilder.append(" ");
        }
        stringBuilder.append("§6- §e");
        stringBuilder.append(getDescription());
        return stringBuilder.toString();
    }
}
