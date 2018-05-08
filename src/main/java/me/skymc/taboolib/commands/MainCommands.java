package me.skymc.taboolib.commands;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.sub.*;
import me.skymc.taboolib.commands.sub.cycle.CycleCommand;
import me.skymc.taboolib.commands.sub.itemlist.ItemListCommand;
import me.skymc.taboolib.commands.sub.shell.ShellCommand;
import me.skymc.taboolib.commands.sub.sounds.SoundsCommand;
import me.skymc.taboolib.inventory.ItemUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Parameter;

public class MainCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.HELP", label);
        } else if ("save".equalsIgnoreCase(args[0])) {
            new SaveCommand(sender, args);
        } else if ("enchants".equalsIgnoreCase(args[0])) {
            new EnchantCommand(sender, args);
        } else if ("potions".equalsIgnoreCase(args[0])) {
            new PotionCommand(sender, args);
        } else if ("flags".equalsIgnoreCase(args[0])) {
            new FlagCommand(sender, args);
        } else if ("attributes".equalsIgnoreCase(args[0])) {
            new AttributesCommand(sender, args);
        } else if ("slots".equalsIgnoreCase(args[0])) {
            new SlotCommand(sender, args);
        } else if ("importdata".equalsIgnoreCase(args[0])) {
            new ImportCommand(sender, args);
        } else if ("iteminfo".equalsIgnoreCase(args[0])) {
            new InfoCommand(sender, args);
        } else if ("itemlist".equalsIgnoreCase(args[0])) {
            new ItemListCommand(sender, args);
        } else if ("item".equalsIgnoreCase(args[0]) || "i".equalsIgnoreCase(args[0])) {
            new ItemCommand(sender, args);
        } else if ("setvariable".equalsIgnoreCase(args[0])) {
            new VariableSetCommand(sender, args);
        } else if ("getvariable".equalsIgnoreCase(args[0])) {
            new VariableGetCommand(sender, args);
        } else if ("shell".equalsIgnoreCase(args[0]) || "s".equalsIgnoreCase(args[0])) {
            new ShellCommand(sender, args);
        } else if ("cycle".equalsIgnoreCase(args[0]) || "c".equalsIgnoreCase(args[0])) {
            new CycleCommand(sender, args);
        } else if ("sounds".equalsIgnoreCase(args[0])) {
            new SoundsCommand(sender, args);
        } else if ("tagprefix".equalsIgnoreCase(args[0])) {
            new TagPrefixCommand(sender, args);
        } else if ("tagsuffix".equalsIgnoreCase(args[0])) {
            new TagSuffixCommand(sender, args);
        } else if ("tagdelete".equalsIgnoreCase(args[0])) {
            new TagDeleteCommand(sender, args);
        } else if ("itemreload".equalsIgnoreCase(args[0]) || "ireload".equalsIgnoreCase(args[0])) {
            ItemUtils.reloadItemCache();
            ItemUtils.reloadItemName();
            TLocale.sendTo(sender, "COMMANDS.RELOAD.SUCCESS-NORMAL");
        } else {
            TLocale.sendTo(sender, "COMMANDS.PARAMETER.UNKNOWN");
        }
        return true;
    }
}
