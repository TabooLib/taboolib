package io.izzel.taboolib.module.command.base;

import io.izzel.taboolib.util.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * 子命令接口
 *
 * @author Bkm016
 * @since 2018-04-17
 */
public abstract class BaseSubCommand {

    private String label;
    private boolean player;
    private SubCommand annotation;
    private BaseMainCommand mainCommand;

    /**
     * 命令执行方法
     * 同 Bukkit 方法
     *
     * @param sender  执行者
     * @param command 命令对象
     * @param label   命令
     * @param args    参数
     */
    abstract public void onCommand(CommandSender sender, Command command, String label, String[] args);

    /**
     * 判定传入参数是否符合参数约束
     *
     * @param args 传入参数
     */
    public final boolean isParameterConform(String[] args) {
        return IntStream.range(0, getArguments().length).noneMatch(i -> getArguments()[i].isRequired() && (args == null || args.length <= i));
    }

    /**
     * 获取命令帮助列表中对展示文本
     *
     * @param sender 执行者
     * @param label  命令
     */
    public final String getCommandString(CommandSender sender, String label) {
        return mainCommand.getDisplay().displayHelp(sender, this, label);
    }

    /**
     * 是否拥有权限
     *
     * @param sender 执行者
     */
    public final boolean hasPermission(CommandSender sender) {
        return Strings.isBlank(getPermission()) || sender.hasPermission(getPermission());
    }

    protected final BaseSubCommand label(String label) {
        this.label = label;
        return this;
    }

    protected final BaseSubCommand player() {
        player = true;
        return this;
    }

    protected final BaseSubCommand annotation(SubCommand annotation) {
        this.annotation = annotation;
        return this;
    }

    protected final BaseSubCommand mainCommand(BaseMainCommand mainCommand) {
        this.mainCommand = mainCommand;
        return this;
    }

    @SafeVarargs
    protected final <T> T[] of(T... argument) {
        return argument;
    }

    public final BaseMainCommand getMainCommand() {
        return mainCommand;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return annotation.description();
    }

    public String[] getAliases() {
        return annotation.aliases();
    }

    public Argument[] getArguments() {
        return Arrays.stream(annotation.arguments()).map(a -> a.endsWith("?") ? new Argument(a.substring(0, a.length() - 1), false) : new Argument(a)).toArray(Argument[]::new);
    }

    public CommandType getType() {
        return player ? CommandType.PLAYER : annotation.type();
    }

    @Deprecated
    public boolean ignoredLabel() {
        return annotation.ignoredLabel();
    }

    @Deprecated
    public boolean requiredPlayer() {
        return annotation.requiredPlayer();
    }

    public String getPermission() {
        return annotation.permission();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hideInHelp() {
        return annotation.hideInHelp();
    }
}