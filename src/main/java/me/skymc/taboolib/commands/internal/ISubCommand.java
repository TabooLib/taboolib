package me.skymc.taboolib.commands.internal;

import me.skymc.taboolib.commands.internal.type.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public interface ISubCommand {

    /**
     * 指令名
     *
     * @return 文本
     */
    String getLabel();

    /**
     * 指令描述
     *
     * @return 文本
     */
    String getDescription();

    /**
     * 指令参数
     *
     * @return {@link CommandArgument}
     */
    CommandArgument[] getArguments();

    /**
     * 指令执行方法
     *
     * @param sender 指令使用者
     * @param command 指令对象
     * @param label 主命令
     * @param args 参数（不含主命令及子命令）
     */
    void onCommand(CommandSender sender, Command command, String label, String[] args);

}
