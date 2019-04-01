package me.skymc.taboolib.commands.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class AttributesCommand extends SubCommand {

	public AttributesCommand(CommandSender sender, String[] args) {
		super(sender, args);

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ATTRIBUTES.HEAD");

        Arrays.stream(new String[]{"damage", "speed", "attackspeed", "health", "knockback", "armor", "luck"}).forEach(attribute -> TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ATTRIBUTES.BODY", attribute));

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ATTRIBUTES.FOOT");
	}
}
