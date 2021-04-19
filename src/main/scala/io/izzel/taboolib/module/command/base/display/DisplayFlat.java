package io.izzel.taboolib.module.command.base.display;

import io.izzel.taboolib.module.command.base.Argument;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.BaseSubCommand;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.TLocaleLoader;
import io.izzel.taboolib.util.Strings;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 扁平化命令帮助列表排版
 * 于 5.X 版本更新，并默认启用该方案
 *
 * @author sky
 * @since 2020-04-04 16:18
 */
public class DisplayFlat extends DisplayBase {

    @Override
    public void displayHead(CommandSender sender, BaseMainCommand main, String label) {
        Plugin plugin = main.getRegisterCommand().getPlugin();
        TLocale.sendTo(sender, "COMMANDS.DISPLAY.FLAT.HEAD", plugin.getName(), plugin.getDescription().getVersion(), main.getRegisterCommand().getName());
    }

    @Override
    public void displayBottom(CommandSender sender, BaseMainCommand main, String label) {
        Plugin plugin = main.getRegisterCommand().getPlugin();
        TLocale.sendTo(sender, "COMMANDS.DISPLAY.FLAT.BOTTOM", plugin.getName(), plugin.getDescription().getVersion(), main.getRegisterCommand().getName());
    }

    @Override
    public void displayParameters(CommandSender sender, BaseSubCommand sub, String label) {
        if (sub != null) {
            if (Strings.nonBlack(sub.getDescription(sender))) {
                TLocale.sendTo(sender, "COMMANDS.DISPLAY.FLAT.PARAMETERS", sub.getLabel(), sub.getDescription(sender), displayHelp(sender, sub, label), "/" + label + " " + sub.getLabel() + " ");
            } else {
                TLocale.sendTo(sender, "COMMANDS.DISPLAY.FLAT.PARAMETERS-EMPTY", sub.getLabel(), sub.getDescription(sender), displayHelp(sender, sub, label), "/" + label + " " + sub.getLabel() + " ");
            }
        }
    }

    @Override
    public void displayErrorUsage(CommandSender sender, BaseMainCommand main, String label, String help) {
        try {
            TLocale.sendTo(sender, "COMMANDS.DISPLAY.FLAT.ERROR-USAGE", label, help, main.getRegisterCommand().getPlugin().getName());
        } catch (NullPointerException t) {
            t.printStackTrace();
        }
    }

    @Override
    public void displayErrorCommand(CommandSender sender, BaseMainCommand main, String label, String help) {
        try {
            TLocale.sendTo(sender, "COMMANDS.DISPLAY.FLAT.ERROR-COMMAND", label, help, main.getRegisterCommand().getPlugin().getName());
        } catch (NullPointerException t) {
            t.printStackTrace();
        }
    }

    @Override
    public String displayHelp(CommandSender sender, BaseSubCommand sub, String label) {
        if (Strings.nonBlack(sub.getDescription(sender))) {
            return TLocale.asString(sender, "COMMANDS.DISPLAY.FLAT.HELP", label, sub.getLabel(), Arrays.stream(sub.getArguments()).map(a -> argument(sub, sender, a)).collect(Collectors.joining(" ")), sub.getDescription(sender));
        } else {
            return TLocale.asString(sender, "COMMANDS.DISPLAY.FLAT.HELP-EMPTY", label, sub.getLabel(), Arrays.stream(sub.getArguments()).map(a -> argument(sub, sender, a)).collect(Collectors.joining(" ")));
        }
    }

    public String argument(BaseSubCommand subCommand, CommandSender sender, Argument argument) {
        String name;
        if (argument.getName().startsWith("@")) {
            name = TLocaleLoader.asString(subCommand.getMainCommand().getRegisterCommand().getPlugin(), sender, argument.getName().substring(1));
        } else {
            name = argument.getName();
        }
        return TLocale.asString(sender, "COMMANDS.DISPLAY.FLAT.ARGUMENT" + (argument.isRequired() ? "REQUIRED" : "OPTIONAL"), name);
    }
}
