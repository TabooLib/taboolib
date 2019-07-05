package io.izzel.taboolib.util.chat;

import java.util.Arrays;

/**
 * @author md_5
 */
public final class HoverEvent {

    private final Action action;
    private final BaseComponent[] value;

    public HoverEvent(Action action, BaseComponent[] value) {
        this.action = action;
        this.value = value;
    }

    public Action getAction() {
        return action;
    }

    public BaseComponent[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "action=" + "HoverEvent{" + action + ", value=" + Arrays.toString(value) + '}';
    }

    public enum Action {

        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM,
        SHOW_ENTITY
    }
}
