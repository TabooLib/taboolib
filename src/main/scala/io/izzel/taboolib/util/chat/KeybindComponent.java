package io.izzel.taboolib.util.chat;

public final class KeybindComponent extends BaseComponent {

    /**
     * The keybind identifier to use.
     * <br>
     * Will be replaced with the actual key the client is using.
     */
    private String keybind;

    /**
     * Creates a keybind component from the original to clone it.
     *
     * @param original the original for the new keybind component.
     */
    public KeybindComponent(KeybindComponent original) {
        super(original);
        setKeybind(original.getKeybind());
    }

    /**
     * Creates a keybind component with the passed internal keybind value.
     *
     * @param keybind the keybind value
     * @see Keybinds
     */
    public KeybindComponent(String keybind) {
        setKeybind(keybind);
    }

    public KeybindComponent() {
    }

    @Override
    public KeybindComponent duplicate() {
        return new KeybindComponent(this);
    }

    @Override
    protected void toPlainText(StringBuilder builder) {
        builder.append(getKeybind());
        super.toPlainText(builder);
    }

    @Override
    protected void toLegacyText(StringBuilder builder) {
        addFormat(builder);
        builder.append(getKeybind());
        super.toLegacyText(builder);
    }

    public String getKeybind() {
        return this.keybind;
    }

    public void setKeybind(String keybind) {
        this.keybind = keybind;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof KeybindComponent)) return false;
        final KeybindComponent other = (KeybindComponent) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        final Object this$keybind = this.getKeybind();
        final Object other$keybind = other.getKeybind();
        return this$keybind == null ? other$keybind == null : this$keybind.equals(other$keybind);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof KeybindComponent;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $keybind = this.getKeybind();
        result = result * PRIME + ($keybind == null ? 43 : $keybind.hashCode());
        return result;
    }

    public String toString() {
        return "KeybindComponent(keybind=" + this.getKeybind() + ")";
    }
}
