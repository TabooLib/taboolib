package me.skymc.taboolib.commands.sub;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.other.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ItemCommand extends SubCommand {

    public ItemCommand(CommandSender sender, String[] args) {
        super(sender, args);

        if (args.length < 2) {
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ITEM.INVALID-NAME");
            return;
        }

        if (ItemUtils.getCacheItem(args[1]) == null) {
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ITEM.INVALID-ITEM", args[1]);
            return;
        }

        Player player;
        Integer amount = 1;
        ItemStack item = ItemUtils.getCacheItem(args[1]).clone();

        if (args.length > 2) {
            player = Bukkit.getPlayerExact(args[2]);
            if (player == null) {
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ITEM.INVALID-PLAYER", args[2]);
                return;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            TLocale.sendTo(sender, "COMMANDS.GLOBAL.ONLY-PLAYER");
            return;
        }

        if (args.length > 3) {
            amount = NumberUtils.getInteger(args[3]);
            if (amount < 1) {
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ITEM.INVALID-NUMBER");
                return;
            }
        }
        item.setAmount(amount);

        HashMap<Integer, ItemStack> map = player.getInventory().addItem(item);
        if (map.size() > 0) {
            player.getWorld().dropItem(player.getLocation(), item);
        }

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ITEM.SUCCESS", player.getName());
        setReturn(true);
    }
}
