package me.skymc.taboolib.commands.internal;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.string.ArrayUtils;
import me.skymc.taboolib.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2018-05-07 21:38
 */
public abstract class InternalCommandExecutor implements InternalCommand, CommandExecutor, TabExecutor {

    private InternalCommandExecutor subExecutor;
    private List<InternalSubCommandExecutor> subCommandExecutors = new ArrayList<>();

    public static InternalCommandExecutor createCommandExecutor(String command, InternalCommandExecutor internalCommandExecutor) {
        assert Bukkit.getPluginCommand(command) == null : "PluginCommand \"" + command + "\"not found";
        assert internalCommandExecutor != null : "Executor can not be null";
        assert internalCommandExecutor.getCommandTitle() != null : "Executor title can not be null";
        assert internalCommandExecutor.getClass() != InternalCommandExecutor.class : "SubExecutor can not be \"InternalCommandExecutor.class\"";
        internalCommandExecutor.setSubExecutor(internalCommandExecutor);
        Bukkit.getPluginCommand(command).setExecutor(internalCommandExecutor);
        Bukkit.getPluginCommand(command).setTabCompleter(internalCommandExecutor);
        return internalCommandExecutor;
    }

    public void setSubExecutor(InternalCommandExecutor subExecutor) {
        this.subExecutor = subExecutor;
    }

    public InternalCommandExecutor getSubExecutor() {
        return subExecutor;
    }

    public List<InternalSubCommandExecutor> getSubCommandExecutors() {
        return subCommandExecutors;
    }

    public void registerSubCommandExecutor(InternalSubCommandExecutor subCommandExecutor) {
        assert subCommandExecutor.getLabel() != null : "Command label can not be null";
        assert subCommandExecutor.getDescription() != null : "Command description can not be null";
        assert subCommandExecutor.getArguments() != null : "Command arguments can not be null";
        subCommandExecutors.add(subCommandExecutor);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            helpCommand(sender, label);
        } else {
            for (InternalSubCommandExecutor subCommand : subCommandExecutors) {
                if (subCommand == null || !args[0].equalsIgnoreCase(subCommand.getLabel())) {
                    continue;
                }
                if (!isConfirmType(sender, subCommand.getType())) {
                    TLocale.sendTo(sender, "COMMANDS.INTERNAL.ONLY-PLAYER", args[0], TLocale.asString("COMMANDS.INTERNAL.TYPE-" + subCommand.getType()));
                    return true;
                }
                String[] subCommandArgs = ArrayUtils.removeFirst(args);
                if (subCommand.isParameterConform(subCommandArgs)) {
                    subCommand.onCommand(sender, command, label, subCommandArgs);
                } else {
                    TLocale.sendTo(sender, "COMMANDS.INTERNAL.ERROR-USAGE", args[0], subCommand.getCommandString(label));
                }
                return true;
            }
            new BukkitRunnable() {

                @Override
                public void run() {
                    List<InternalSubCommandExecutor> commandCompute = subCommandExecutors.stream().filter(Objects::nonNull).sorted((b, a) -> Double.compare(StringUtils.similarDegree(args[0], a.getLabel()), StringUtils.similarDegree(args[0], b.getLabel()))).collect(Collectors.toList());
                    if (commandCompute.size() > 0) {
                        TLocale.sendTo(sender, "COMMANDS.INTERNAL.ERROR-COMMAND", args[0], commandCompute.get(0).getCommandString(label).trim());
                    }
                }
            }.runTaskAsynchronously(Main.getInst());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        return args.length == 1 ? subCommandExecutors.stream().filter(internalCommandExecutor -> internalCommandExecutor != null && (args[0].isEmpty() || internalCommandExecutor.getLabel().startsWith(args[0]))).map(InternalSubCommand::getLabel).collect(Collectors.toList()) : null;
    }

    private String getEmptyLine() {
        return TabooLib.getVerint() < 10800 ? "~" : "";
    }

    private void helpCommand(CommandSender sender, String label) {
        sender.sendMessage(getEmptyLine());
        sender.sendMessage(subExecutor.getCommandTitle());
        sender.sendMessage(getEmptyLine());
        subCommandExecutors.stream().map(subCommand -> subCommand == null ? getEmptyLine() : subCommand.getCommandString(label)).forEach(sender::sendMessage);
        sender.sendMessage(getEmptyLine());
    }

    private boolean isConfirmType(CommandSender sender, InternalSubCommandType commandType) {
        return commandType == InternalSubCommandType.ALL || sender instanceof ConsoleCommandSender && commandType == InternalSubCommandType.CONSOLE;
    }
}
