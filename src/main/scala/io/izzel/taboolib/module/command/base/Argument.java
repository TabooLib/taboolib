package io.izzel.taboolib.module.command.base;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * BaseSubCommand 命令参数
 *
 * @author Bkm016
 * @since 2018-04-17
 */
public class Argument {

    // 参数名称
    private final String name;
    // 是否必须
    private boolean required;
    // 参数补全
    private CommandTab tab;
    // 参数约束
    private ArgumentType restrict;

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public CommandTab getTab() {
        return tab;
    }

    public ArgumentType getRestrict() {
        return restrict;
    }

    public Argument(String name) {
        this(name, true);
    }

    public Argument(String name, CommandTab tab) {
        this(name, true, tab);
    }

    public Argument(String name, boolean required) {
        this(name, required, null);
    }

    public Argument(String name, boolean required, CommandTab tab) {
        this.name = name;
        this.required = required;
        this.tab = tab;
    }

    /**
     * 将该参数定义为可选参数
     * 帮助列表中当显示文本将会被修改
     */
    public Argument optional() {
        this.required = false;
        return this;
    }

    /**
     * 参数补全
     */
    public Argument complete(CommandTab tab) {
        this.tab = tab;
        return this;
    }

    /**
     * 参数补全
     */
    public Argument complete(@Nullable List<String> tab) {
        this.tab = () -> tab;
        return this;
    }

    /**
     * 参数约束（5.43 update）
     */
    public Argument restrict(@Nullable ArgumentType restrict) {
        this.restrict = restrict;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Argument)) {
            return false;
        }
        Argument that = (Argument) o;
        return isRequired() == that.isRequired() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(tab, that.tab);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), isRequired(), tab);
    }

    public static Argument[] of(String expression) {
        return Arrays.stream(expression.split("[,;]")).map(s -> s.endsWith("?") ? new Argument(s.substring(0, s.length() - 1), false) : new Argument(s)).toArray(Argument[]::new);
    }
}
