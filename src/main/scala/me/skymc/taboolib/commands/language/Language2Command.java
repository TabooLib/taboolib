package me.skymc.taboolib.commands.language;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.commands.builder.SimpleCommandBuilder;
import me.skymc.taboolib.common.loader.Instantiable;
import me.skymc.taboolib.string.language2.Language2Value;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author sky
 * @since 2018年2月13日 下午5:11:01
 */
@Instantiable("Language2Command")
public class Language2Command {

    public Language2Command() {
        SimpleCommandBuilder.create("language2", TabooLib.instance())
                .aliases("lang2")
                .permission("taboolib.admin")
                .execute((sender, args) -> {
                    if (args.length == 0) {
                        TLocale.sendTo(sender, "COMMANDS.LANGUAGE2.HELP", "langauge2");
                    } else if ("reload".equalsIgnoreCase(args[0])) {
                        reload(sender);
                    } else if ("send".equalsIgnoreCase(args[0])) {
                        send(sender, args);
                    }
                    return true;
                }).build();
    }

    private void send(CommandSender sender, String[] args) {
        if (args.length < 3) {
            TLocale.sendTo(sender, "COMMANDS.PARAMETER.UNKNOWN");
        } else {
            long time = System.currentTimeMillis();
            Language2Value value = getLanguage2Value(args);

            if ("ALL".equalsIgnoreCase(args[1])) {
                value.broadcast();
            } else {
                Player player = Bukkit.getPlayerExact(args[1]);
                if (player == null) {
                    TLocale.sendTo(sender, "COMMANDS.LANGUAGE2.INVALID-PLAYER", args[1]);
                } else {
                    value.send(player);
                }
            }

            if (sender instanceof Player && ((Player) sender).getItemInHand().getType().equals(Material.COMMAND)) {
                TLocale.sendTo(sender, "COMMANDS.LANGUAGE2.SUCCESS-SEND", String.valueOf(System.currentTimeMillis() - time));
            }
        }
    }

    private Language2Value getLanguage2Value(String[] args) {
        Language2Value value = Main.getExampleLanguage2().get(args[2]);
        if (args.length > 3) {
            int i = 0;
            for (String variable : args[3].split("\\|")) {
                value.addPlaceholder("$" + i++, variable);
            }
        }
        return value;
    }

    private void reload(CommandSender sender) {
        TLocale.sendTo(sender, "COMMANDS.RELOAD.LOADING");
        long time = System.currentTimeMillis();
        Main.getExampleLanguage2().reload();
        TLocale.sendTo(sender, "COMMANDS.RELOAD.SUCCESS-ELAPSED-TIME", String.valueOf(System.currentTimeMillis() - time));
    }
}
