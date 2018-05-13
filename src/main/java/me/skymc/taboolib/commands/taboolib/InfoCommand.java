package me.skymc.taboolib.commands.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.itemnbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand extends SubCommand {

    @SuppressWarnings("deprecation")
    public InfoCommand(CommandSender sender, String[] args) {
        super(sender, args);
        if (isPlayer()) {
            Player player = (Player) sender;
            if (player.getItemInHand().getType().equals(Material.AIR)) {
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.INFO.INVALID-ITEM");
            } else {
                NBTItem nbt = new NBTItem(player.getItemInHand());
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.INFO.ITEM-INFO",
                        player.getItemInHand().getType().name(),
                        ItemUtils.getCustomName(player.getItemInHand()),
                        player.getItemInHand().getTypeId() + ":" + player.getItemInHand().getDurability(),
                        nbt.toString());
            }
        }
    }
}
