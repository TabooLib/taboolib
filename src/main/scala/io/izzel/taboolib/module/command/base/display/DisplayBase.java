package io.izzel.taboolib.module.command.base.display;

import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.BaseSubCommand;
import org.bukkit.command.CommandSender;

/**
 * 命令帮助列表排版接口
 *
 * @Author sky
 * @Since 2020-04-04 16:14
 */
public abstract class DisplayBase {

    /**
     * 帮助头
     *
     * @param sender          执行者
     * @param baseMainCommand 主命令对象
     * @param label           命令
     */
    abstract public void displayHead(CommandSender sender, BaseMainCommand baseMainCommand, String label);

    /**
     * 帮助底部
     *
     * @param sender          执行者
     * @param baseMainCommand 主命令对象
     * @param label           命令
     */
    abstract public void displayBottom(CommandSender sender, BaseMainCommand baseMainCommand, String label);

    /**
     * 帮助文本
     *
     * @param sender         执行者
     * @param baseSubCommand 子命令对象
     * @param label          命令
     */
    abstract public String displayHelp(CommandSender sender, BaseSubCommand baseSubCommand, String label);

    /**
     * 参数文本
     *
     * @param sender         执行者
     * @param baseSubCommand 子命令对象
     * @param label          命令
     */
    abstract public void displayParameters(CommandSender sender, BaseSubCommand baseSubCommand, String label);

    /**
     * 错误用法提示
     *
     * @param sender          执行者
     * @param baseMainCommand 主命令对象
     * @param label           命令
     * @param help            帮助
     */
    abstract public void displayErrorUsage(CommandSender sender, BaseMainCommand baseMainCommand, String label, String help);

    /**
     * 错误命令提示
     *
     * @param sender          执行者
     * @param baseMainCommand 主命令对象
     * @param label           命令
     * @param help            帮助
     */
    abstract public void displayErrorCommand(CommandSender sender, BaseMainCommand baseMainCommand, String label, String help);
}
