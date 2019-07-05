package io.izzel.taboolib.module.command.base;

import io.izzel.taboolib.locale.TLocale;

import java.util.Objects;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public class CommandArgument {

    private String name;
    private boolean required;
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

    public CommandArgument(String name) {
        this(name, true);
    }

    public CommandArgument(String name, CommandTab tab) {
        this(name, true, tab);
    }

    public CommandArgument(String name, boolean required) {
        this(name, required, null);
    }

    public CommandArgument(String name, boolean required, CommandTab tab) {
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
        if (!(o instanceof CommandArgument)) {
            return false;
        }
        CommandArgument that = (CommandArgument) o;
        return isRequired() == that.isRequired() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(tab, that.tab);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), isRequired(), tab);
    }
}
