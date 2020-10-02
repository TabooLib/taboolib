package io.izzel.taboolib.module.command.base;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public class Argument {

    // 参数名称
    private final String name;
    // 是否必须
    private final boolean required;
    // 参数补全
    private final CommandTab tab;

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public CommandTab getTab() {
        return tab;
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
