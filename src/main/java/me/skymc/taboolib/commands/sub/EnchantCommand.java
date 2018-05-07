package me.skymc.taboolib.commands.sub;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.Arrays;

public class EnchantCommand extends SubCommand {

    @SuppressWarnings("deprecation")
    public EnchantCommand(CommandSender sender, String[] args) {
        super(sender, args);

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ENCHANTS.HEAD");

        Arrays.stream(Enchantment.values()).forEach(enchant -> TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ENCHANTS.BODY", String.valueOf(enchant.getId()), enchant.getName()));

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ENCHANTS.FOOT");
    }
}
