package me.skymc.taboolib.commands.sub;

import com.ilummc.tlib.resources.TLocale;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;

import java.util.Arrays;

public class FlagCommand extends SubCommand {

	public FlagCommand(CommandSender sender, String[] args) {
		super(sender, args);

		TLocale.sendTo(sender, "COMMANDS.TABOOLIB.FLAGS.HEAD");

		Arrays.stream(ItemFlag.values()).forEach(itemFlag -> TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ENCHANTS.BODY", itemFlag.name()));

		TLocale.sendTo(sender, "COMMANDS.TABOOLIB.FLAGS.FOOT");
	}
}
