package me.skymc.taboolib.commands.internal.type;

import java.util.Objects;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public class CommandArgument {

    private String name;
    private boolean required;

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public CommandArgument(String name) {
        this(name, true);
    }

    public CommandArgument(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    @Override
    public String toString() {
        return required ? "§7[§8" + name + "§7]" : "§7<§8" + name + "§7>";
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
        return Objects.equals(getName(), that.getName()) && isRequired() == that.isRequired();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), isRequired());
    }
}
