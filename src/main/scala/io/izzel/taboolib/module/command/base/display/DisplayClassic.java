package io.izzel.taboolib.module.command.base.display;

import io.izzel.taboolib.module.command.base.Argument;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.BaseSubCommand;
import io.izzel.taboolib.module.locale.TLocale;
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
        if (Strings.nonBlack(sub.getDescription())) {
            return TLocale.asString("COMMANDS.DISPLAY.CLASSIC.HELP", label, sub.getLabel(), Arrays.stream(sub.getArguments()).map(a -> argument(sub, a)).collect(Collectors.joining(" ")), sub.getDescription());
        } else {
            return TLocale.asString("COMMANDS.DISPLAY.CLASSIC.HELP-EMPTY", label, sub.getLabel(), Arrays.stream(sub.getArguments()).map(a -> argument(sub, a)).collect(Collectors.joining(" ")));
        }
    }

    public String argument(BaseSubCommand subCommand, Argument argument) {
        return argument.isRequired() ? TLocale.asString("COMMANDS.DISPLAY.CLASSIC.ARGUMENT-REQUIRED", argument.getName()) : TLocale.asString("COMMANDS.DISPLAY.CLASSIC.ARGUMENT-OPTIONAL", argument.getName());
    }
}
