package me.skymc.taboolib.commands.sub;

import com.ilummc.tlib.resources.TLocale;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;

import java.util.Arrays;

public class AttributesCommand extends SubCommand {

	public AttributesCommand(CommandSender sender, String[] args) {
		super(sender, args);

		TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ATTRIBUTES.HEAD");

		Arrays.stream(new String[] { "damage", "speed", "attackspeed", "health", "knockback", "armor", "luck" }).forEach(attribute -> TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ATTRIBUTES.BODY", attribute));

		TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ATTRIBUTES.FOOT");
	}
}
