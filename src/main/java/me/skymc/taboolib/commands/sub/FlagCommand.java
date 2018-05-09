package me.skymc.taboolib.commands.sub;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;

public class FlagCommand extends SubCommand {

	public FlagCommand(CommandSender sender, String[] args) {
		super(sender, args);

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.FLAGS.HEAD");

        Arrays.stream(ItemFlag.values()).forEach(itemFlag -> TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ENCHANTS.BODY", itemFlag.name()));

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.FLAGS.FOOT");
	}
}
