package io.izzel.taboolib.module.command.base;

import io.izzel.taboolib.module.locale.TLocale;

import java.util.Objects;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public class Argument {

    // 参数名称
    private String name;
    // 是否必须
    private boolean required;
    // 参数补全
    private CommandTab tab;

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
    public String toString() {
        return required ? TLocale.asString("COMMANDS.INTERNAL.COMMAND-ARGUMENT-REQUIRE", name) : TLocale.asString("COMMANDS.INTERNAL.COMMAND-ARGUMENT", name);
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
}
