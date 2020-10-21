package io.izzel.taboolib.module.command.base;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
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

    public Argument optional() {
        this.required = false;
        return this;
    }

    public Argument complete(CommandTab tab) {
        this.tab = tab;
        return this;
    }

    public Argument complete(List<String> tab) {
        this.tab = () -> tab;
        return this;
    }

    public Argument restrict(ArgumentType restrict) {
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
