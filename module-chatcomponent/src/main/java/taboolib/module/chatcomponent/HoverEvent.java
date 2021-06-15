package taboolib.module.chatcomponent;

public final class HoverEvent {

    private final Action action;
    private final BaseComponent[] value;

    public HoverEvent(Action action, BaseComponent[] value) {
        this.action = action;
        this.value = value;
    }

    public Action getAction() {
        return this.action;
    }

    public BaseComponent[] getValue() {
        return this.value;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof HoverEvent)) return false;
        final HoverEvent other = (HoverEvent) o;
        final Object this$action = this.getAction();
        final Object other$action = other.getAction();
        if (this$action == null ? other$action != null : !this$action.equals(other$action)) return false;
        return java.util.Arrays.deepEquals(this.getValue(), other.getValue());
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $action = this.getAction();
        result = result * PRIME + ($action == null ? 43 : $action.hashCode());
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getValue());
        return result;
    }

    public String toString() {
        return "HoverEvent(action=" + this.getAction() + ", value=" + java.util.Arrays.deepToString(this.getValue()) + ")";
    }

    public enum Action {

        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM,
        SHOW_ENTITY
    }
}
