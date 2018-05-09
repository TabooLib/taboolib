package me.skymc.taboolib.commands.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

public class PotionCommand extends SubCommand {

	@SuppressWarnings("deprecation")
	public PotionCommand(CommandSender sender, String[] args) {
		super(sender, args);

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.POTIONS.HEAD");

        for (PotionEffectType potionEffectType : PotionEffectType.values()) {
            if (potionEffectType != null) {
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.POTIONS.BODY", String.valueOf(potionEffectType.getId()), potionEffectType.getName());
            }
        }

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.POTIONS.FOOT");
	}
}
