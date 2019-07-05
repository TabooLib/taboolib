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
public class SimpleCommandBuilder {

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

    SimpleCommandBuilder(String command, Plugin plugin) {
        this.command = command;
        this.plugin = plugin;
        this.description = "";
        this.usage = "/" + command;
        this.aliases = new ArrayList<>();
        this.build = false;
    }

    public static SimpleCommandBuilder create(String command, Plugin plugin) {
        return new SimpleCommandBuilder(command.toLowerCase(), plugin);
    }

    public SimpleCommandBuilder command(String command) {
        this.command = command;
        return this;
    }

    public SimpleCommandBuilder plugin(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

    public SimpleCommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    public SimpleCommandBuilder usage(String usage) {
        this.usage = usage;
        return this;
    }

    public SimpleCommandBuilder aliases(String... aliases) {
        this.aliases = ArrayUtil.asList(aliases);
        return this;
    }

    public SimpleCommandBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public SimpleCommandBuilder permissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
        return this;
    }

    public SimpleCommandBuilder execute(CompleterCommand completerCommand) {
        this.completerCommand = completerCommand;
        return this;
    }

    public SimpleCommandBuilder tab(CompleterTab completerTab) {
        this.completerTab = completerTab;
        return this;
    }

    public SimpleCommandBuilder forceRegister() {
        this.forceRegister = true;
        return this;
    }

    public SimpleCommandBuilder build() {
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
