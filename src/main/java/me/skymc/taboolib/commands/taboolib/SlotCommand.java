package me.skymc.taboolib.commands.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class SlotCommand extends SubCommand {

	public SlotCommand(CommandSender sender, String[] args) {
		super(sender, args);

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SLOTS.HEAD");

        Arrays.stream(new String[]{"mainhand", "offhand", "feet", "legs", "chest", "head", "all"}).forEach(slots -> TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SLOTS.BODY", slots));

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SLOTS.FOOT");
	}
}
