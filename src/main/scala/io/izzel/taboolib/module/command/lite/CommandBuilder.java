package io.izzel.taboolib.module.command.lite;

import com.google.common.base.Preconditions;
import io.izzel.taboolib.module.command.TCommandHandler;
import io.izzel.taboolib.util.ArrayUtil;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author sky
 * @Since 2018-08-27 8:42
 * @BuilderLevel 1.0
 */
public class CommandBuilder {

    public static final CompleterTab EMPTY_COMPLETER_TAB = ((sender, args) -> new ArrayList<>());
    public static final CompleterCommand EMPTY_COMPLETER_COMMAND = ((sender, args) -> {});

    private String command;
    private Plugin plugin;
    private String description;
    private String usage;
    private List<String> aliases;
    private String permission;
    private String permissionMessage;
    private CompleterTab completerTab = EMPTY_COMPLETER_TAB;
    private CompleterCommand completerCommand = EMPTY_COMPLETER_COMMAND;
    private boolean forceRegister;
    private boolean build;

    CommandBuilder(String command, Plugin plugin) {
        this.command = command;
        this.plugin = plugin;
        this.description = "";
        this.usage = "/" + command;
        this.aliases = new ArrayList<>();
        this.build = false;
    }

    public static CommandBuilder create(String command, Plugin plugin) {
        return new CommandBuilder(command.toLowerCase(), plugin);
    }

    public CommandBuilder command(String command) {
        this.command = command;
        return this;
    }

    public CommandBuilder plugin(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

    public CommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    public CommandBuilder usage(String usage) {
        this.usage = usage;
        return this;
    }

    public CommandBuilder aliases(String... aliases) {
        this.aliases = ArrayUtil.asList(aliases);
        return this;
    }

    public CommandBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public CommandBuilder permissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
        return this;
    }

    public CommandBuilder execute(CompleterCommand completerCommand) {
        this.completerCommand = completerCommand;
        return this;
    }

    public CommandBuilder tab(CompleterTab completerTab) {
        this.completerTab = completerTab;
        return this;
    }

    public CommandBuilder forceRegister() {
        this.forceRegister = true;
        return this;
    }

    public CommandBuilder build() {
        Preconditions.checkNotNull(plugin, "缺少 \"plugin\" 部分");
        Preconditions.checkNotNull(command, "缺少 \"command\" 部分");
        Preconditions.checkNotNull(completerCommand, "缺少 \"CompleterCommand\" 部分");
        Preconditions.checkNotNull(completerTab, "缺少 \"CompleterTab\" 部分");
        if (forceRegister) {
            TCommandHandler.getKnownCommands().remove(command);
        }
        TCommandHandler.registerPluginCommand(
                plugin,
                command,
                description,
                usage,
                aliases,
                permission,
                permissionMessage,
                (sender, command, s, args) -> {
                    completerCommand.execute(sender, args);
                    return true;
                },
                (sender, command, s, args) -> {
                    try {
                        return completerTab.execute(sender, args);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    return null;
                });
        build = true;
        return this;
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public String getCommand() {
        return command;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getPermission() {
        return permission;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public CompleterTab getCompleterTab() {
        return completerTab;
    }

    public CompleterCommand getCompleterCommand() {
        return completerCommand;
    }

    public boolean isForceRegister() {
        return forceRegister;
    }

    public boolean isBuild() {
        return build;
    }
}
