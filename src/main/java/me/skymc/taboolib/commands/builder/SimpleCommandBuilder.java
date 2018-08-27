package me.skymc.taboolib.commands.builder;

import com.google.common.base.Preconditions;
import me.skymc.taboolib.commands.builder.type.CompleterCommand;
import me.skymc.taboolib.commands.builder.type.CompleterTab;
import me.skymc.taboolib.commands.internal.TCommandHandler;
import me.skymc.taboolib.string.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author sky
 * @Since 2018-08-27 8:42
 * @BuilderLevel 1.0
 */
public class SimpleCommandBuilder {

    private final String command;
    private final Plugin plugin;
    private String description;
    private String usage;
    private List<String> aliases;
    private String permission;
    private String permissionMessage;
    private CompleterCommand completerCommand;
    private CompleterTab completerTab;

    SimpleCommandBuilder(String command, Plugin plugin) {
        this.command = command;
        this.plugin = plugin;
        this.description = "";
        this.usage = "/" + command;
        this.aliases = new ArrayList<>();
    }

    public static SimpleCommandBuilder create(String command, Plugin plugin) {
        return new SimpleCommandBuilder(command, plugin);
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
        this.aliases = ArrayUtils.asList(aliases);
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

    public SimpleCommandBuilder build() {
        Preconditions.checkNotNull(completerCommand, "缺少 \"CompleterCommand\" 部分");
        TCommandHandler.registerPluginCommand(
                plugin,
                command,
                description,
                usage,
                aliases,
                permission,
                permissionMessage,
                (sender, command, s, args) -> completerCommand.execute(sender, args),
                (sender, command, s, args) -> completerTab.execute(sender, args));
        return this;
    }
}
