package me.skymc.taboolib.commands.internal;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.internal.type.CommandArgument;
import me.skymc.taboolib.commands.internal.type.CommandType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public abstract class BaseSubCommand {

    /**
     * 指令名
     *
     * @return 文本
     */
    abstract public String getLabel();

    /**
     * 指令描述
     *
     * @return 文本
     */
    abstract public String getDescription();

    /**
     * 指令参数
     *
     * @return {@link CommandArgument}
     */
    abstract public CommandArgument[] getArguments();

    /**
     * 指令执行方法
     *
     * @param sender  指令使用者
     * @param command 指令对象
     * @param label   主命令
     * @param args    参数（不含主命令及子命令）
     */
    abstract public void onCommand(CommandSender sender, Command command, String label, String[] args);

    /**
     * 指令执行者
     *
     * @return {@link CommandType}
     */
    public CommandType getType() {
        return CommandType.ALL;
    }

    /**
     * 参数是否屏蔽子命令名
     *
     * @return boolean
     */
    public boolean ignoredLabel() {
        return true;
    }

    /**
     * 是否需要玩家在线
     *
     * @return boolean
     */
    public boolean requiredPlayer() {
        return false;
    }

    /**
     * 需要权限
     *
     * @return boolean
     */
    public String getPermission() {
        return null;
    }

    /**
     * 参数是否符合
     *
     * @param args 参数
     * @return boolean
     */
    public boolean isParameterConform(String[] args) {
        return IntStream.range(0, getArguments().length).noneMatch(i -> getArguments()[i].isRequired() && (args == null || args.length <= i));
    }

    /**
     * 获取帮助文本
     *
     * @param label 子命令标题
     * @return String
     */
    public String getCommandString(String label) {
        String stringBuilder = Arrays.stream(getArguments()).map(parameter -> parameter.toString() + " ").collect(Collectors.joining());
        return TLocale.asString("COMMANDS.INTERNAL.COMMAND-HELP", label, getLabel(), stringBuilder.trim(), getDescription());
    }
}