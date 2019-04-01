package me.skymc.taboolib.commands.taboolib;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.message.ChatCatcher;
import me.skymc.taboolib.message.ChatCatcher.Catcher;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.playerdata.DataUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author sky
 */
public class SaveCommand extends SubCommand {

    public SaveCommand(CommandSender sender, String[] args) {
        super(sender, args);
        if (!(sender instanceof Player)) {
            TLocale.sendTo(sender, "COMMANDS.GLOBAL.ONLY-PLAYER");
            return;
        }

        if (args.length < 2) {
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SAVE.INVALID-NAME");
            return;
        }

        if (((Player) sender).getItemInHand().getType().equals(Material.AIR)) {
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SAVE.INVALID-ITEM");
            return;
        }

        if (ItemUtils.getItemCachesFinal().containsKey(args[1])) {
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SAVE.INVALID-ITEM-FINAL-EXISTS");
            return;
        }

        if (ItemUtils.getItemCaches().containsKey(args[1])) {
            // 检查聊天引导
            if (ChatCatcher.contains((Player) sender)) {
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SAVE.GUIDE-EXISTS");
                return;
            }

            ChatCatcher.call((Player) sender, new Catcher() {

                @Override
                public void cancel() {
                    TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SAVE.GUIDE-QUIT");
                }

                @Override
                public Catcher before() {
                    TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SAVE.GUIDE-BEFORE", args[1]);
                    return this;
                }

                @SuppressWarnings("deprecation")
                @Override
                public boolean after(String message) {
                    if ("yes".equalsIgnoreCase(message)) {
                        saveItem(args[1], ((Player) sender).getItemInHand());
                        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SAVE.SUCCESS", args[1]);
                    } else {
                        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.SAVE.GUIDE-QUIT");
                    }
                    return false;
                }
            });
        } else {
            saveItem(args[1], ((Player) sender).getItemInHand());
            MsgUtils.send(sender, "物品 &f" + args[1] + " &7已保存");
        }
    }

    private void saveItem(String name, ItemStack item) {
        FileConfiguration conf = ConfigUtils.load(Main.getInst(), ItemUtils.getItemCacheFile());
        conf.set(name + ".bukkit", item);
        DataUtils.saveConfiguration(conf, ItemUtils.getItemCacheFile());
        ItemUtils.reloadItemCache();
    }
}
