package io.izzel.taboolib.util.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author md_5
 */
public abstract class BaseComponent {

    BaseComponent parent;

    /**
     * The color of this component and any child components (unless overridden)
     */
    private ChatColor color;
    /**
     * Whether this component and any child components (unless overridden) is
     * bold
     */
    private Boolean bold;
    /**
     * Whether this component and any child components (unless overridden) is
     * italic
     */
    private Boolean italic;
    /**
     * Whether this component and any child components (unless overridden) is
     * underlined
     */
    private Boolean underlined;
    /**
     * Whether this component and any child components (unless overridden) is
     * strikethrough
     */
    private Boolean strikethrough;
    /**
     * Whether this component and any child components (unless overridden) is
     * obfuscated
     */
    private Boolean obfuscated;
    /**
     * The text to insert into the chat when this component (and child
     * components) are clicked while pressing the shift key
     */
    private String insertion;

    /**
     * Appended components that inherit this component's formatting and events
     */
    private List<BaseComponent> extra;

    /**
     * The action to perform when this component (and child components) are
     * clicked
     */
    private ClickEvent clickEvent;
    /**
     * The action to perform when this component (and child components) are
     * hovered over
     */
    private HoverEvent hoverEvent;

    public String getInsertion() {
        return insertion;
    }

    public List<BaseComponent> getExtra() {
        return extra;
    }

    public ClickEvent getClickEvent() {
        return clickEvent;
    }

    public HoverEvent getHoverEvent() {
        return hoverEvent;
    }

    public void setParent(BaseComponent parent) {
        this.parent = parent;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    public void setItalic(Boolean italic) {
        this.italic = italic;
    }

    public void setUnderlined(Boolean underlined) {
        this.underlined = underlined;
    }

    public void setStrikethrough(Boolean strikethrough) {
        this.strikethrough = strikethrough;
    }

    public void setObfuscated(Boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    public void setInsertion(String insertion) {
        this.insertion = insertion;
    }

    public void setClickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
    }

    public void setHoverEvent(HoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
    }

    @Override
    public String toString() {
        return "parent=" + "BaseComponent{" + parent + ", color=" + color + ", bold=" + bold + ", italic=" + italic + ", underlined=" + underlined + ", strikethrough=" + strikethrough + ", obfuscated=" + obfuscated + ", insertion='" + insertion + '\'' + ", extra=" + extra + ", clickEvent=" + clickEvent + ", hoverEvent=" + hoverEvent + '}';
    }

    BaseComponent() {
    }

    BaseComponent(BaseComponent old) {
        copyFormatting(old, ComponentBuilder.FormatRetention.ALL, true);

        if (old.getExtra() != null) {
            for (BaseComponent extra : old.getExtra()) {
                addExtra(extra.duplicate());
            }
        }
    }

    /**
     * Copies the events and formatting of a BaseComponent. Already set
     * formatting will be replaced.
     *
     * @param component the component to copy from
     */
    public void copyFormatting(BaseComponent component) {
        copyFormatting(component, ComponentBuilder.FormatRetention.ALL, true);
    }

    /**
     * Copies the events and formatting of a BaseComponent.
     *
     * @param component the component to copy from
     * @param replace   if already set formatting should be replaced by the new
     *                  component
     */
    public void copyFormatting(BaseComponent component, boolean replace) {
        copyFormatting(component, ComponentBuilder.FormatRetention.ALL, replace);
    }

    /**
     * Copies the specified formatting of a BaseComponent.
     *
     * @param component the component to copy from
     * @param retention the formatting to copy
     * @param replace   if already set formatting should be replaced by the new
     *                  component
     */
    public void copyFormatting(BaseComponent component, ComponentBuilder.FormatRetention retention, boolean replace) {
        if (retention == ComponentBuilder.FormatRetention.EVENTS || retention == ComponentBuilder.FormatRetention.ALL) {
            if (replace || clickEvent == null) {
                setClickEvent(component.getClickEvent());
            }
            if (replace || hoverEvent == null) {
                setHoverEvent(component.getHoverEvent());
            }
        }
        if (retention == ComponentBuilder.FormatRetention.FORMATTING || retention == ComponentBuilder.FormatRetention.ALL) {
            if (replace || color == null) {
                setColor(component.getColorRaw());
            }
            if (replace || bold == null) {
                setBold(component.isBoldRaw());
            }
            if (replace || italic == null) {
                setItalic(component.isItalicRaw());
            }
            if (replace || underlined == null) {
                setUnderlined(component.isUnderlinedRaw());
            }
            if (replace || strikethrough == null) {
                setStrikethrough(component.isStrikethroughRaw());
            }
            if (replace || obfuscated == null) {
                setObfuscated(component.isObfuscatedRaw());
            }
            if (replace || insertion == null) {
                setInsertion(component.getInsertion());
            }
        }
    }

    /**
     * Retains only the specified formatting.
     *
     * @param retention the formatting to retain
     */
    public void retain(ComponentBuilder.FormatRetention retention) {
        if (retention == ComponentBuilder.FormatRetention.FORMATTING || retention == ComponentBuilder.FormatRetention.NONE) {
            setClickEvent(null);
            setHoverEvent(null);
        }
        if (retention == ComponentBuilder.FormatRetention.EVENTS || retention == ComponentBuilder.FormatRetention.NONE) {
            setColor(null);
            setBold(null);
            setItalic(null);
            setUnderlined(null);
            setStrikethrough(null);
            setObfuscated(null);
            setInsertion(null);
        }
    }

    /**
     * Clones the BaseComponent and returns the clone.
     *
     * @return The duplicate of this BaseComponent
     */
    public abstract BaseComponent duplicate();

    /**
     * Clones the BaseComponent without formatting and returns the clone.
     *
     * @return The duplicate of this BaseComponent
     * @deprecated API use discouraged, use traditional duplicate
     */
    @Deprecated
    public BaseComponent duplicateWithoutFormatting() {
        BaseComponent component = duplicate();
        component.retain(ComponentBuilder.FormatRetention.NONE);
        return component;
    }

    /**
     * Converts the components to a string that uses the old formatting codes
     * ({@link ChatColor#COLOR_CHAR}
     *
     * @param components the components to convert
     * @return the string in the old format
     */
    public static String toLegacyText(BaseComponent... components) {
        StringBuilder builder = new StringBuilder();
        for (BaseComponent msg : components) {
            builder.append(msg.toLegacyText());
        }
        return builder.toString();
    }

    /**
     * Converts the components into a string without any formatting
     *
     * @param components the components to convert
     * @return the string as plain text
     */
    public static String toPlainText(BaseComponent... components) {
        StringBuilder builder = new StringBuilder();
        for (BaseComponent msg : components) {
            builder.append(msg.toPlainText());
        }
        return builder.toString();
    }

    /**
     * Returns the color of this component. This uses the parent's color if this
     * component doesn't have one. {@link ChatColor#WHITE}
     * is returned if no color is found.
     *
     * @return the color of this component
     */
    public ChatColor getColor() {
        if (color == null) {
            if (parent == null) {
                return ChatColor.WHITE;
            }
            return parent.getColor();
        }
        return color;
    }

    /**
     * Returns the color of this component without checking the parents color.
     * May return null
     *
     * @return the color of this component
     */
    public ChatColor getColorRaw() {
        return color;
    }

    /**
     * Returns whether this component is bold. This uses the parent's setting if
     * this component hasn't been set. false is returned if none of the parent
     * chain has been set.
     *
     * @return whether the component is bold
     */
    public boolean isBold() {
        if (bold == null) {
            return parent != null && parent.isBold();
        }
        return bold;
    }

    /**
     * Returns whether this component is bold without checking the parents
     * setting. May return null
     *
     * @return whether the component is bold
     */
    public Boolean isBoldRaw() {
        return bold;
    }

    /**
     * Returns whether this component is italic. This uses the parent's setting
     * if this component hasn't been set. false is returned if none of the
     * parent chain has been set.
     *
     * @return whether the component is italic
     */
    public boolean isItalic() {
        if (italic == null) {
            return parent != null && parent.isItalic();
        }
        return italic;
    }

    /**
     * Returns whether this component is italic without checking the parents
     * setting. May return null
     *
     * @return whether the component is italic
     */
    public Boolean isItalicRaw() {
        return italic;
    }

    /**
     * Returns whether this component is underlined. This uses the parent's
     * setting if this component hasn't been set. false is returned if none of
     * the parent chain has been set.
     *
     * @return whether the component is underlined
     */
    public boolean isUnderlined() {
        if (underlined == null) {
            return parent != null && parent.isUnderlined();
        }
        return underlined;
    }

    /**
     * Returns whether this component is underlined without checking the parents
     * setting. May return null
     *
     * @return whether the component is underlined
     */
    public Boolean isUnderlinedRaw() {
        return underlined;
    }

    /**
     * Returns whether this component is strikethrough. This uses the parent's
     * setting if this component hasn't been set. false is returned if none of
     * the parent chain has been set.
     *
     * @return whether the component is strikethrough
     */
    public boolean isStrikethrough() {
        if (strikethrough == null) {
            return parent != null && parent.isStrikethrough();
        }
        return strikethrough;
    }

    /**
     * Returns whether this component is strikethrough without checking the
     * parents setting. May return null
     *
     * @return whether the component is strikethrough
     */
    public Boolean isStrikethroughRaw() {
        return strikethrough;
    }

    /**
     * Returns whether this component is obfuscated. This uses the parent's
     * setting if this component hasn't been set. false is returned if none of
     * the parent chain has been set.
     *
     * @return whether the component is obfuscated
     */
    public boolean isObfuscated() {
        if (obfuscated == null) {
            return parent != null && parent.isObfuscated();
        }
        return obfuscated;
    }

    /**
     * Returns whether this component is obfuscated without checking the parents
     * setting. May return null
     *
     * @return whether the component is obfuscated
     */
    public Boolean isObfuscatedRaw() {
        return obfuscated;
    }

    public void setExtra(List<BaseComponent> components) {
        components.forEach(component -> component.parent = this);
        extra = components;
    }

    /**
     * Appends a text element to the component. The text will inherit this
     * component's formatting
     *
     * @param text the text to append
     */
    public void addExtra(String text) {
        addExtra(new TextComponent(text));
    }

    /**
     * Appends a component to the component. The text will inherit this
     * component's formatting
     *
     * @param component the component to append
     */
    public void addExtra(BaseComponent component) {
        if (extra == null) {
            extra = new ArrayList<>();
        }
        component.parent = this;
        extra.add(component);
    }

    /**
     * Returns whether the component has any formatting or events applied to it
     *
     * @return Whether any formatting or events are applied
     */
    public boolean hasFormatting() {
        return color != null || italic != null || bold != null || underlined != null || strikethrough != null || obfuscated != null || insertion != null || hoverEvent != null || clickEvent != null;
    }

    /**
     * Converts the component into a string without any formatting
     *
     * @return the string as plain text
     */
    public String toPlainText() {
        StringBuilder builder = new StringBuilder();
        toPlainText(builder);
        return builder.toString();
    }

    void toPlainText(StringBuilder builder) {
        if (extra != null) {
            extra.forEach(e -> e.toPlainText(builder));
        }
    }

    /**
     * Converts the component to a string that uses the old formatting codes
     * ({@link ChatColor#COLOR_CHAR}
     *
     * @return the string in the old format
     */
    public String toLegacyText() {
        StringBuilder builder = new StringBuilder();
        toLegacyText(builder);
        return builder.toString();
    }

    void toLegacyText(StringBuilder builder) {
        if (extra != null) {
            extra.forEach(e -> e.toLegacyText(builder));
        }
    }
}
