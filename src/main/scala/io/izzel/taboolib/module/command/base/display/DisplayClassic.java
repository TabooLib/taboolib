package io.izzel.taboolib.module.command.base.display;

import io.izzel.taboolib.module.command.base.Argument;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.BaseSubCommand;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.TLocaleLoader;
import io.izzel.taboolib.util.Strings;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 经典命令帮助列表排版
 *
 * @author sky
 * @since 2020-04-04 16:17
 */
public class DisplayClassic extends DisplayBase {

    @Override
    public void displayHead(CommandSender sender, BaseMainCommand main, String label) {
        sender.sendMessage(" ");
        sender.sendMessage(main.getCommandTitle());
        sender.sendMessage(" ");
    }

    @Override
    public void displayBottom(CommandSender sender, BaseMainCommand main, String label) {
        sender.sendMessage(" ");
    }

    @Override
    public void displayParameters(CommandSender sender, BaseSubCommand sub, String label) {
        sender.sendMessage(" " + displayHelp(sender, sub, label));
    }

    @Override
    public void displayErrorUsage(CommandSender sender, BaseMainCommand main, String label, String help) {
        TLocale.sendTo(sender, "COMMANDS.DISPLAY.CLASSIC.ERROR-USAGE", label, help, main.getRegisterCommand().getPlugin().getName());
    }

    @Override
    public void displayErrorCommand(CommandSender sender, BaseMainCommand main, String label, String help) {
        TLocale.sendTo(sender, "COMMANDS.DISPLAY.CLASSIC.ERROR-COMMAND", label, help, main.getRegisterCommand().getPlugin().getName());
    }

    @Override
    public String displayHelp(CommandSender sender, BaseSubCommand sub, String label) {
        if (Strings.nonBlack(sub.getDescription(sender))) {
            return TLocale.asString(sender, "COMMANDS.DISPLAY.CLASSIC.HELP", label, sub.getLabel(), Arrays.stream(sub.getArguments()).map(a -> argument(sub, sender, a)).collect(Collectors.joining(" ")), sub.getDescription(sender));
        } else {
            return TLocale.asString(sender, "COMMANDS.DISPLAY.CLASSIC.HELP-EMPTY", label, sub.getLabel(), Arrays.stream(sub.getArguments()).map(a -> argument(sub, sender, a)).collect(Collectors.joining(" ")));
        }
    }

    public String argument(BaseSubCommand subCommand, CommandSender sender, Argument argument) {
        String name;
        if (argument.getName().startsWith("@")) {
            name = TLocaleLoader.asString(subCommand.getMainCommand().getRegisterCommand().getPlugin(), sender, argument.getName().substring(1));
        } else {
            name = argument.getName();
        }
        return TLocale.asString(sender, "COMMANDS.DISPLAY.CLASSIC.ARGUMENT-" + (argument.isRequired() ? "REQUIRED" : "OPTIONAL"), name);
    }
}
