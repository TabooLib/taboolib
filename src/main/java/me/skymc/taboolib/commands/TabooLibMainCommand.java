package me.skymc.taboolib.commands;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.commands.internal.BaseMainCommand;
import me.skymc.taboolib.commands.internal.BaseSubCommand;
import me.skymc.taboolib.commands.internal.TCommand;
import me.skymc.taboolib.commands.internal.type.CommandArgument;
import me.skymc.taboolib.commands.internal.type.CommandRegister;
import me.skymc.taboolib.commands.internal.type.CommandType;
import me.skymc.taboolib.commands.taboolib.*;
import me.skymc.taboolib.database.GlobalDataManager;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.other.DateUtils;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.timecycle.TimeCycle;
import me.skymc.taboolib.timecycle.TimeCycleEvent;
import me.skymc.taboolib.timecycle.TimeCycleInitializeEvent;
import me.skymc.taboolib.timecycle.TimeCycleManager;
import me.skymc.taboolib.update.UpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

/**
 * @Author sky
 * @Since 2018-05-09 21:38
 */
@TCommand(
        name = "taboolib",
        permission = "taboolib.admin",
        aliases = "tlib"
)
public class TabooLibMainCommand extends BaseMainCommand {

    @Override
    public String getCommandTitle() {
        return TLocale.asString("COMMANDS.TABOOLIB.COMMAND-TITLE");
    }

    @CommandRegister(priority = 1)
    BaseSubCommand save = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "save";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.SAVE.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.SAVE.ARGUMENTS.0"))};
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new SaveCommand(sender, args);
        }

        @Override
        public boolean ignoredLabel() {
            return false;
        }
    };

    @CommandRegister(priority = 2)
    BaseSubCommand item = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "item";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.ITEM.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.ITEM.ARGUMENTS.0")),
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.ITEM.ARGUMENTS.1"), false),
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.ITEM.ARGUMENTS.2"), false)
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new ItemCommand(sender, args);
        }

        @Override
        public boolean ignoredLabel() {
            return false;
        }
    };

    @CommandRegister(priority = 3)
    BaseSubCommand itemInfo = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "itemInfo";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.ITEMLIST.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new InfoCommand(sender, args);
        }
    };

    @CommandRegister(priority = 3.1)
    BaseSubCommand infoList = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "itemList";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.ITEMLIST.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new ItemListCommand(sender, args);
        }

        @Override
        public boolean ignoredLabel() {
            return false;
        }
    };

    @CommandRegister(priority = 4)
    BaseSubCommand itemReload = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "itemReload";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.ITEMRELOAD.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            ItemUtils.reloadItemCache();
            ItemUtils.reloadItemName();
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.ITEMRELOAD.SUCCESS-RELOAD");
        }
    };

    @CommandRegister(priority = 5)
    BaseSubCommand emptyLine1 = null;

    @CommandRegister(priority = 6)
    BaseSubCommand attributes = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "attributes";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.ATTRIBUTES.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new AttributesCommand(sender, args);
        }
    };

    @CommandRegister(priority = 7)
    BaseSubCommand enchants = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "enchants";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.ENCHANTS.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new EnchantCommand(sender, args);
        }
    };

    @CommandRegister(priority = 8)
    BaseSubCommand potions = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "potions";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.POTIONS.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new PotionCommand(sender, args);
        }
    };

    @CommandRegister(priority = 9)
    BaseSubCommand flags = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "flags";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.FLAGS.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new FlagCommand(sender, args);
        }
    };

    @CommandRegister(priority = 10)
    BaseSubCommand slots = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "slots";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.SLOTS.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new AttributesCommand(sender, args);
        }
    };

    @CommandRegister(priority = 11)
    BaseSubCommand sounds = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "sounds";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.SOUNDS.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new SoundsCommand(sender, args);
        }

        @Override
        public boolean ignoredLabel() {
            return false;
        }
    };

    @CommandRegister(priority = 12)
    BaseSubCommand emptyLine2 = null;

    @CommandRegister(priority = 13)
    BaseSubCommand getVariable = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "getVariable";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.VARIABLE.DESCRIPTION.GET");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.VARIABLE.ARGUMENTS.GET.0")),
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.VARIABLE.ARGUMENTS.GET.1"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new VariableGetCommand(sender, args);
        }

        @Override
        public boolean ignoredLabel() {
            return false;
        }
    };

    @CommandRegister(priority = 13.1)
    BaseSubCommand setVariable = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "setVariable";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.VARIABLE.DESCRIPTION.SET");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.VARIABLE.ARGUMENTS.SET.0")),
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.VARIABLE.ARGUMENTS.SET.1")),
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.VARIABLE.ARGUMENTS.SET.2"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new VariableSetCommand(sender, args);
        }

        @Override
        public boolean ignoredLabel() {
            return false;
        }
    };

    @CommandRegister(priority = 13.2)
    BaseSubCommand getEmptyLine3 = null;

    @CommandRegister(priority = 13.3)
    BaseSubCommand cycleList = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "cycleList";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.TIMECYCLE.DESCRIPTION.LIST");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.LIST.HEAD");
            TimeCycleManager.getTimeCycles().forEach(cycle -> TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.LIST.BODY", cycle.getName()));
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.LIST.FOOT");
        }
    };

    @CommandRegister(priority = 14)
    BaseSubCommand cycleInfo = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "cycleInfo";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.TIMECYCLE.DESCRIPTION.INFO");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.TIMECYCLE.ARGUMENTS.INFO.0"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            TimeCycle cycle = TimeCycleManager.getTimeCycle(args[0]);
            if (cycle == null) {
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.INVALID-CYCLE", args[0]);
            } else {
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.CYCLE-INFO",
                        asString(cycle.getCycle() / 1000L),
                        cycle.getPlugin().getName(),
                        DateUtils.CH_ALL.format(TimeCycleManager.getBeforeTimeline(cycle.getName())),
                        DateUtils.CH_ALL.format(TimeCycleManager.getAfterTimeline(cycle.getName())));
            }
        }

        private String asString(long seconds) {
            long day = TimeUnit.SECONDS.toDays(seconds);
            long hours = TimeUnit.SECONDS.toHours(seconds) - day * 24;
            long minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60L;
            long second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60L;
            return "§f" + day + "§7 天, §f" + hours + "§7 小时, §f" + minute + "§7 分钟, §f" + second + "§7 秒";
        }
    };

    @CommandRegister(priority = 15)
    BaseSubCommand cycleReset = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "cycleReset";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.TIMECYCLE.DESCRIPTION.RESET");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.TIMECYCLE.ARGUMENTS.RESET.0"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            TimeCycle cycle = TimeCycleManager.getTimeCycle(args[0]);
            if (cycle == null) {
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.INVALID-CYCLE", args[0]);
                return;
            }
            new BukkitRunnable() {

                @Override
                public void run() {
                    long time = new TimeCycleInitializeEvent(cycle, System.currentTimeMillis()).call().getTimeline();
                    // 初始化
                    GlobalDataManager.setVariable("timecycle:" + cycle.getName(), String.valueOf(time));
                    // 触发器
                    Bukkit.getPluginManager().callEvent(new TimeCycleEvent(cycle));
                    // 提示
                    TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.CYCLE-RESET", args[0]);
                }
            }.runTaskAsynchronously(Main.getInst());
        }
    };

    @CommandRegister(priority = 16)
    BaseSubCommand cycleUpdate = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "cycleUpdate";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.TIMECYCLE.DESCRIPTION.UPDATE");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.TIMECYCLE.ARGUMENTS.UPDATE.0"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            TimeCycle cycle = TimeCycleManager.getTimeCycle(args[0]);
            if (cycle == null) {
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.INVALID-CYCLE", args[0]);
                return;
            }
            new BukkitRunnable() {

                @Override
                public void run() {
                    // 重置
                    GlobalDataManager.setVariable("timecycle:" + cycle.getName(), String.valueOf(System.currentTimeMillis()));
                    // 触发器
                    Bukkit.getPluginManager().callEvent(new TimeCycleEvent(cycle));
                    // 提示
                    TLocale.sendTo(sender, "COMMANDS.TABOOLIB.TIMECYCLE.CYCLE-UPDATE", args[0]);
                }
            }.runTaskAsynchronously(Main.getInst());
        }
    };

    @CommandRegister(priority = 17)
    BaseSubCommand getEmptyLine4 = null;

    @CommandRegister(priority = 20)
    BaseSubCommand tagDisplay = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "tagDisplay";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.PLAYERTAG.DESCRIPTION.DISPLAY");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.PLAYERTAG.ARGUMENTS.DISPLAY.0")),
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.PLAYERTAG.ARGUMENTS.DISPLAY.1"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new TagDisplayCommand(sender, args);
        }

        @Override
        public boolean ignoredLabel() {
            return false;
        }
    };

    @CommandRegister(priority = 21)
    BaseSubCommand tagPrefix = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "tagPrefix";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.PLAYERTAG.DESCRIPTION.PREFIX");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.PLAYERTAG.ARGUMENTS.PREFIX.0")),
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.PLAYERTAG.ARGUMENTS.PREFIX.1"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new TagPrefixCommand(sender, args);
        }

        @Override
        public boolean ignoredLabel() {
            return false;
        }
    };

    @CommandRegister(priority = 22)
    BaseSubCommand tagSuffix = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "tagSuffix";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.PLAYERTAG.DESCRIPTION.SUFFIX");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.PLAYERTAG.ARGUMENTS.SUFFIX.0")),
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.PLAYERTAG.ARGUMENTS.SUFFIX.1"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new TagSuffixCommand(sender, args);
        }

        @Override
        public boolean ignoredLabel() {
            return false;
        }
    };

    @CommandRegister(priority = 23)
    BaseSubCommand tagDelete = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "tagDelete";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.PLAYERTAG.DESCRIPTION.DELETE");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[]{
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.PLAYERTAG.ARGUMENTS.DELETE.0"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new TagDeleteCommand(sender, args);
        }

        @Override
        public boolean ignoredLabel() {
            return false;
        }
    };

    @CommandRegister(priority = 24)
    BaseSubCommand getEmptyLine6 = null;

    @CommandRegister(priority = 24.1)
    BaseSubCommand lagServer = new BaseSubCommand() {
        @Override
        public String getLabel() {
            return "lagServer";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.LAGSERVER.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[] {
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.LAGSERVER.ARGUMENTS.0"))
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (NumberUtils.getInteger(args[0]) > 300000) {
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.LAGSERVER.INVALID-TIME");
            } else {
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.LAGSERVER.START");
                try {
                    Thread.sleep(NumberUtils.getInteger(args[0]));
                } catch (Exception ignored) {
                }
                TLocale.sendTo(sender, "COMMANDS.TABOOLIB.LAGSERVER.STOP");
            }
        }
    };

    @CommandRegister(priority = 26)
    BaseSubCommand getEmptyLine7 = null;

    @CommandRegister(priority = 27)
    BaseSubCommand importData = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "importData";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.IMPORTDATA.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            new ImportCommand(sender, args);
        }
    };

    @CommandRegister(priority = 28)
    BaseSubCommand updatePlugin = new BaseSubCommand() {

        @Override
        public String getLabel() {
            return "updatePlugin";
        }

        @Override
        public String getDescription() {
            return TLocale.asString("COMMANDS.TABOOLIB.UPDATEPLUGIN.DESCRIPTION");
        }

        @Override
        public CommandArgument[] getArguments() {
            return new CommandArgument[] {
                    new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.UPDATEPLUGIN.ARGUMENTS.0"), false)
            };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {
            UpdateTask.updatePlugin(true, args.length > 0);
        }

        @Override
        public CommandType getType() {
            return CommandType.CONSOLE;
        }
    };
}
