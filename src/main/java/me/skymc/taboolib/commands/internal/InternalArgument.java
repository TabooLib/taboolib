package me.skymc.taboolib.commands.internal;

/**
 * @author Bkm016
 * @since 2018-04-17
 */
public class InternalArgument {

    private String name;
    private boolean required;

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public InternalArgument(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    @Override
    public String toString() {
        return required ? "§7[§8" + name + "§7]" : "§7<§8" + name + "§7>";
    }
}
