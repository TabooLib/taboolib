package me.skymc.taboolib.commands.sub;

import com.ilummc.tlib.resources.TLocale;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.jsonformatter.JSONFormatter;
import me.skymc.taboolib.jsonformatter.click.SuggestCommandEvent;
import me.skymc.taboolib.jsonformatter.hover.ShowTextEvent;

import java.util.Arrays;

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
