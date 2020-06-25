package io.izzel.taboolib.util.chat;

public final class ClickEvent {

    /**
     * The type of action to perform on click.
     */
    private final Action action;
    /**
     * Depends on the action.
     *
     * @see Action
     */
    private final String value;

    public ClickEvent(Action action, String value) {
        this.action = action;
        this.value = value;
    }

    public Action getAction() {
        return this.action;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ClickEvent)) return false;
        final ClickEvent other = (ClickEvent) o;
        final Object this$action = this.getAction();
        final Object other$action = other.getAction();
        if (this$action == null ? other$action != null : !this$action.equals(other$action)) return false;
        final Object this$value = this.getValue();
        final Object other$value = other.getValue();
        return this$value == null ? other$value == null : this$value.equals(other$value);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $action = this.getAction();
        result = result * PRIME + ($action == null ? 43 : $action.hashCode());
        final Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        return result;
    }

    public String toString() {
        return "ClickEvent(action=" + this.getAction() + ", value=" + this.getValue() + ")";
    }

    public enum Action {

        /**
         * Open a url at the path given by
         * {@link ClickEvent#value}.
         */
        OPEN_URL,
        /**
         * Open a file at the path given by
         * {@link ClickEvent#value}.
         */
        OPEN_FILE,
        /**
         * Run the command given by
         * {@link ClickEvent#value}.
         */
        RUN_COMMAND,
        /**
         * Inserts the string given by
         * {@link ClickEvent#value} into the player's
         * text box.
         */
        SUGGEST_COMMAND,
        /**
         * Change to the page number given by
         * {@link ClickEvent#value} in a book.
         */
        CHANGE_PAGE,
        /**
         * Copy the string given by
         * {@link ClickEvent#value} into the player's
         * clipboard.
         */
        COPY_TO_CLIPBOARD
    }
}
