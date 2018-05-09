package me.skymc.taboolib.commands;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.internal.BaseMainCommand;
import me.skymc.taboolib.commands.internal.BaseSubCommand;
import me.skymc.taboolib.commands.internal.type.CommandArgument;
import me.skymc.taboolib.commands.internal.type.CommandRegister;
import me.skymc.taboolib.commands.taboolib.*;
import me.skymc.taboolib.inventory.ItemUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @Author sky
 * @Since 2018-05-09 21:38
 */
public class TabooLibMainCommand extends BaseMainCommand {

    @Override
    public String getCommandTitle() {
        return TLocale.asString("COMMANDS.TABOOLIB.COMMAND-TITLE");
    }

    @CommandRegister(priority = 1)
    void save() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 2)
    void item() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 3)
    void itemInfo() {
        registerSubCommand(new BaseSubCommand() {

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
                new ItemListCommand(sender, args);
            }
        });
    }

    @CommandRegister(priority = 4)
    void itemReload() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 5)
    void emptyLine1() {
        registerSubCommand(null);
    }

    @CommandRegister(priority = 6)
    void attributes() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 7)
    void enchants() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 8)
    void potions() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 9)
    void flags() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 10)
    void slots() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 11)
    void sounds() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 12)
    void emptyLine2() {
        registerSubCommand(null);
    }

    @CommandRegister(priority = 13)
    void getVariable() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 13.1)
    void setVariable() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 13.2)
    void emptyLine3() {
        registerSubCommand(null);
    }

    @CommandRegister(priority = 13.3)
    void cycleList() {
        registerSubCommand(new BaseSubCommand() {

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
                new CycleListCommand(sender, args);
            }
        });
    }

    @CommandRegister(priority = 14)
    void cycleInfo() {
        registerSubCommand(new BaseSubCommand() {

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
                new CycleInfoCommand(sender, args);
            }

            @Override
            public boolean ignoredLabel() {
                return false;
            }
        });
    }

    @CommandRegister(priority = 15)
    void cycleReset() {
        registerSubCommand(new BaseSubCommand() {

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
                new CycleResetCommand(sender, args);
            }

            @Override
            public boolean ignoredLabel() {
                return false;
            }
        });
    }

    @CommandRegister(priority = 16)
    void cycleUpdate() {
        registerSubCommand(new BaseSubCommand() {

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
                new CycleUpdateCommand(sender, args);
            }

            @Override
            public boolean ignoredLabel() {
                return false;
            }
        });
    }

    @CommandRegister(priority = 17)
    void emptyLine4() {
        registerSubCommand(null);
    }

    @CommandRegister(priority = 18)
    void shellLoad() {
        registerSubCommand(new BaseSubCommand() {

            @Override
            public String getLabel() {
                return "shellLoad";
            }

            @Override
            public String getDescription() {
                return TLocale.asString("COMMANDS.TABOOLIB.JAVASHELL.DESCRIPTION.LOAD");
            }

            @Override
            public CommandArgument[] getArguments() {
                return new CommandArgument[]{
                        new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.JAVASHELL.ARGUMENTS.LOAD.0"))
                };
            }

            @Override
            public void onCommand(CommandSender sender, Command command, String label, String[] args) {
                new ShellLoadCommand(sender, args);
            }

            @Override
            public boolean ignoredLabel() {
                return false;
            }
        });
    }

    @CommandRegister(priority = 19)
    void shellUnload() {
        registerSubCommand(new BaseSubCommand() {

            @Override
            public String getLabel() {
                return "shellUnload";
            }

            @Override
            public String getDescription() {
                return TLocale.asString("COMMANDS.TABOOLIB.JAVASHELL.DESCRIPTION.UNLOAD");
            }

            @Override
            public CommandArgument[] getArguments() {
                return new CommandArgument[]{
                        new CommandArgument(TLocale.asString("COMMANDS.TABOOLIB.JAVASHELL.ARGUMENTS.UNLOAD.0"))
                };
            }

            @Override
            public void onCommand(CommandSender sender, Command command, String label, String[] args) {
                new ShellUnloadCommand(sender, args);
            }

            @Override
            public boolean ignoredLabel() {
                return false;
            }
        });
    }

    @CommandRegister(priority = 20)
    void emptyLine5() {
        registerSubCommand(null);
    }

    @CommandRegister(priority = 20.5)
    void tagDisplay() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 21)
    void tagPrefix() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 22)
    void tagSuffix() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 23)
    void tagDelete() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }

    @CommandRegister(priority = 24)
    void emptyLine6() {
        registerSubCommand(null);
    }

    @CommandRegister(priority = 25)
    void importData() {
        registerSubCommand(new BaseSubCommand() {

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
        });
    }
}
