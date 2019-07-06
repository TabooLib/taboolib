package io.izzel.taboolib.util.lite;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.command.base.BaseCommand;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.BaseSubCommand;
import io.izzel.taboolib.module.command.base.CommandArgument;
import io.izzel.taboolib.module.command.base.CommandRegister;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;

/**
 * @author sky
 */
@BaseCommand(
        name = "tabooliblogs",
        aliases = {"tlog", "tlogs"},
        permission = "taboolib.admin"
)
public class Logs extends BaseMainCommand {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");

    @Override
    public String getCommandTitle() {
        return TLocale.asString("COMMANDS.TLOGS.COMMAND-TITLE");
    }

    @CommandRegister(priority = 0)
    BaseSubCommand info = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "info";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TLOGS.INFO.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[] {
                    new CommandArgument(TLocale.asString("COMMANDS.TLOGS.INFO.ARGUMENTS.0")),
                    new CommandArgument(TLocale.asString("COMMANDS.TLOGS.INFO.ARGUMENTS.1"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            info(args[0], args[1]);
            if (sender instanceof Player) {
                TLocale.sendTo(sender, "COMMANDS.TLOGS.INFO.SUCCESS");
            }
        }
    };

    @CommandRegister(priority = 1)
    BaseSubCommand error = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "error";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TLOGS.ERROR.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[] {
                    new CommandArgument(TLocale.asString("COMMANDS.TLOGS.ERROR.ARGUMENTS.0")),
                    new CommandArgument(TLocale.asString("COMMANDS.TLOGS.ERROR.ARGUMENTS.1"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            info(args[0], args[1]);
            if (sender instanceof Player) {
                TLocale.sendTo(sender, "COMMANDS.TLOGS.ERROR.SUCCESS");
            }
        }
    };

    @CommandRegister(priority = 2)
    BaseSubCommand warning = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "warning";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TLOGS.WARNING.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[] {
                    new CommandArgument(TLocale.asString("COMMANDS.TLOGS.WARNING.ARGUMENTS.0")),
                    new CommandArgument(TLocale.asString("COMMANDS.TLOGS.WARNING.ARGUMENTS.1"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            info(args[0], args[1]);
            if (sender instanceof Player) {
                TLocale.sendTo(sender, "COMMANDS.TLOGS.WARNING.SUCCESS");
            }
        }
    };

    public static void info(String filePath, String text) {
        info(new File(!filePath.contains(".") ? filePath + ".txt" : filePath), text);
    }

    public static void info(File file, String text) {
        write(file, "[{0} INFO]: {1}\n", text);
    }

    public static void error(String filePath, String text) {
        info(new File(!filePath.contains(".") ? filePath + ".txt" : filePath), text);
    }

    public static void error(File file, String text) {
        write(file, "[{0} ERROR]: {1}\n", text);
    }

    public static void warning(String filePath, String text) {
        info(new File(!filePath.contains(".") ? filePath + ".txt" : filePath), text);
    }

    public static void warning(File file, String text) {
        write(file, "[{0} WARNING]: {1}\n", text);
    }

    public static void write(File file, String format, String text) {
        Bukkit.getScheduler().runTask(TabooLib.getPlugin(), () -> {
            try (FileWriter writer = new FileWriter(Files.file(file), true)) {
                writer.write(Strings.replaceWithOrder(format, dateFormat.format(System.currentTimeMillis()), text));
            } catch (Exception ignored) {
            }
        });
    }
}
