package me.skymc.taboolib.commands.internal;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public abstract class InternalCommandExecutor implements InternalCommand {

    public InternalCommandType getType() {
        return InternalCommandType.ALL;
    }

    public boolean requiredPlayer() {
        return false;
    }

    public boolean isParameterConform(String[] args) {
        return IntStream.range(0, getArguments().length).noneMatch(i -> getArguments()[i].isRequired() && (args == null || args.length <= i));
    }

    public boolean isConfirmType(CommandSender sender, InternalCommandType commandType) {
        return commandType == InternalCommandType.ALL || sender instanceof ConsoleCommandSender && commandType == InternalCommandType.CONSOLE;
    }

    public List<String> getTabCompleter(List<InternalCommandExecutor> internalCommandExecutors, String[] args) {
        return args.length == 1 ? internalCommandExecutors.stream().filter(internalCommandExecutor -> internalCommandExecutor != null && (args[0].isEmpty() || internalCommandExecutor.getLabel().startsWith(args[0]))).map(InternalCommand::getLabel).collect(Collectors.toList()) : null;
    }

    public String getCommandString(String label) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("§f /");
        stringBuilder.append(label);
        stringBuilder.append(" ");
        stringBuilder.append(getLabel());
        stringBuilder.append(" ");
        for (InternalCommandArgument parameter : getArguments()) {
            stringBuilder.append(parameter.toString());
            stringBuilder.append(" ");
        }
        stringBuilder.append("§6- §e");
        stringBuilder.append(getDescription());
        return stringBuilder.toString();
    }
}
