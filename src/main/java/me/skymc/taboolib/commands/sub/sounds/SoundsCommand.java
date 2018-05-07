package me.skymc.taboolib.commands.sub.sounds;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.commands.sub.sounds.listener.SoundsLibraryPatch;
import me.skymc.taboolib.other.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author sky
 * @since 2018-03-18 21:02:26
 */
public class SoundsCommand extends SubCommand {

    public SoundsCommand(CommandSender sender, String[] args) {
        super(sender, args);
        if (isPlayer()) {
            if (args.length == 1) {
                SoundsLibraryPatch.openInventory((Player) sender, 1, null);
            } else if (args.length == 2) {
                SoundsLibraryPatch.openInventory((Player) sender, NumberUtils.getInteger(args[1]), null);
            } else {
                SoundsLibraryPatch.openInventory((Player) sender, NumberUtils.getInteger(args[1]), args[2]);
            }
        }
    }
}
