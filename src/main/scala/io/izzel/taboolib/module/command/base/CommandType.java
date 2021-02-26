package io.izzel.taboolib.module.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * 子命令执行者
 *
 * @author Bkm016
 * @since 2018-04-17
 */
public enum CommandType {

    /**
     * 控制台
     * 继承自 ConsoleCommandSender
     */
    CONSOLE,

    /**
     * 玩家
     * 继承自 Player
     */
    PLAYER,

    /**
     * 所有
     */
    ALL;

    public boolean isType(CommandSender sender) {
        switch (this) {
            case CONSOLE:
                return sender instanceof ConsoleCommandSender;
            case PLAYER:
                return sender instanceof Player;
            default:
                return true;
        }
    }
}
