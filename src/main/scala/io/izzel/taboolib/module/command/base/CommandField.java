package io.izzel.taboolib.module.command.base;

import java.lang.reflect.Field;

/**
 * @author sky
 * @since 2018-05-23 3:07
 */
public class CommandField {

    private final Field field;
    private final Class<?> parent;

    public CommandField(Field field, Class<?> parent) {
        this.field = field;
        this.parent = parent;
    }

    public Field getField() {
        return field;
    }

    public Class<?> getParent() {
        return parent;
    }
}