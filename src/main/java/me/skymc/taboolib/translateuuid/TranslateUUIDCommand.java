package me.skymc.taboolib.translateuuid;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.internal.BaseMainCommand;
import me.skymc.taboolib.commands.internal.BaseSubCommand;
import me.skymc.taboolib.commands.internal.TCommand;
import me.skymc.taboolib.commands.internal.type.CommandArgument;
import me.skymc.taboolib.commands.internal.type.CommandRegister;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @Author sky
 * @Since 2018-06-22 17:09
 */
@TCommand(name = "translateuuid")
public class TranslateUUIDCommand extends BaseMainCommand {

    @Override
    public String getCommandTitle() {
        return TLocale.asString("COMMANDS.TRANSLATE-UUID.COMMAND-TITLE");
    }

    @CommandRegister
    BaseSubCommand importLocal = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "importLocal";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TRANSLATE-UUID.IMPORTLOCAL.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!TranslateUUID.isEnabled()) {
                TLocale.sendTo(sender, "COMMANDS.TRANSLATE-UUID.IMPORTLOCAL.DISABLED");
                return;
            }

            TLocale.sendTo(sender, "COMMANDS.TRANSLATE-UUID.IMPORTLOCAL.SUCCESS");
            TranslateUUID.importLocal();
        }
    };

    @CommandRegister
    BaseSubCommand reload = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "reload";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TRANSLATE-UUID.RELOAD.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            TLocale.sendTo(sender, "COMMANDS.TRANSLATE-UUID.RELOAD.SUCCESS");
            TranslateUUID.cancel();
            TranslateUUID.init();
        }
    };
}
