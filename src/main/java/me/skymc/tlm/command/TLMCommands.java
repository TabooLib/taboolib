package me.skymc.tlm.command;

import me.skymc.tlm.TLM;
import me.skymc.tlm.command.sub.TLMInvCommand;
import me.skymc.tlm.command.sub.TLMKitCommand;
import me.skymc.tlm.command.sub.TLMListCommand;
import me.skymc.tlm.command.sub.TLMReloadCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author sky
 * @since 2018年2月18日 上午12:02:08
 */
public class TLMCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission("taboolib.admin")) {
                TLM.getInst().getLanguage().get("COMMAND-HELP").send(sender);
            } else {
                TLM.getInst().getLanguage().get("NOPERMISSION-HELP").send(sender);
            }
        }

        // 重载
        else if ("reload".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission("taboolib.admin")) {
                new TLMReloadCommand(sender, args);
            } else {
                TLM.getInst().getLanguage().get("NOPERMISSION-RELOAD").send(sender);
            }
        }

        // 列出
        else if ("list".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission("taboolib.admin")) {
                new TLMListCommand(sender, args);
            } else {
                TLM.getInst().getLanguage().get("NOPERMISSION-LIST").send(sender);
            }
        }

        // InventorySave 模块
        else if ("inv".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission("taboolib.admin")) {
                new TLMInvCommand(sender, args);
            } else {
                TLM.getInst().getLanguage().get("NOPERMISSION-INV").send(sender);
            }
        }

        // Kit 模块
        else if ("kit".equalsIgnoreCase(args[0])) {
            new TLMKitCommand(sender, args);
        } else {
            TLM.getInst().getLanguage().get("COMMAND-ERROR").send(sender);
        }
        return true;
    }
}
