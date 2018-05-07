package me.skymc.taboolib.commands.sub;

import com.ilummc.tlib.resources.TLocale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;

import java.util.Arrays;

public class SlotCommand extends SubCommand {

	public SlotCommand(CommandSender sender, String[] args) {
		super(sender, args);

		TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SLOTS.HEAD");

		Arrays.stream(new String[] { "mainhand", "offhand", "feet", "legs", "chest", "head", "all" }).forEach(slots -> TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SLOTS.BODY", slots));

		TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SLOTS.FOOT");
	}
}
