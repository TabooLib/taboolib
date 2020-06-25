package io.izzel.taboolib.util.chat;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseComponent {

    BaseComponent parent;

    /**
     * The color of this component and any child components (unless overridden)
     */
    private ChatColor color;
    /**
     * The font of this component and any child components (unless overridden)
     */
    private String font;
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

    /**
     * Default constructor.
     *
     * @deprecated for use by internal classes only, will be removed.
     */
    @Deprecated
    public BaseComponent() {
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
            if (replace || font == null) {
                setFont(component.getFontRaw());
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
     * Returns the font of this component. This uses the parent's font if this
     * component doesn't have one.
     *
     * @return the font of this component, or null if default font
     */
    public String getFont() {
        if (color == null) {
            if (parent == null) {
                return null;
            }
            return parent.getFont();
        }
        return font;
    }

    /**
     * Returns the font of this component without checking the parents font. May
     * return null
     *
     * @return the font of this component
     */
    public String getFontRaw() {
        return font;
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
        for (BaseComponent component : components) {
            component.parent = this;
        }
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
            extra = new ArrayList<BaseComponent>();
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
        return color != null || font != null || bold != null
                || italic != null || underlined != null
                || strikethrough != null || obfuscated != null
                || insertion != null || hoverEvent != null || clickEvent != null;
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
            for (BaseComponent e : extra) {
                e.toPlainText(builder);
            }
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
            for (BaseComponent e : extra) {
                e.toLegacyText(builder);
            }
        }
    }

    void addFormat(StringBuilder builder) {
        builder.append(getColor());
        if (isBold()) {
            builder.append(ChatColor.BOLD);
        }
        if (isItalic()) {
            builder.append(ChatColor.ITALIC);
        }
        if (isUnderlined()) {
            builder.append(ChatColor.UNDERLINE);
        }
        if (isStrikethrough()) {
            builder.append(ChatColor.STRIKETHROUGH);
        }
        if (isObfuscated()) {
            builder.append(ChatColor.MAGIC);
        }
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public void setFont(String font) {
        this.font = font;
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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BaseComponent)) return false;
        final BaseComponent other = (BaseComponent) o;
        if (!other.canEqual(this)) return false;
        final Object this$color = this.getColor();
        final Object other$color = other.getColor();
        if (this$color == null ? other$color != null : !this$color.equals(other$color)) return false;
        final Object this$font = this.getFont();
        final Object other$font = other.getFont();
        if (this$font == null ? other$font != null : !this$font.equals(other$font)) return false;
        final Object this$bold = this.bold;
        final Object other$bold = other.bold;
        if (this$bold == null ? other$bold != null : !this$bold.equals(other$bold)) return false;
        final Object this$italic = this.italic;
        final Object other$italic = other.italic;
        if (this$italic == null ? other$italic != null : !this$italic.equals(other$italic)) return false;
        final Object this$underlined = this.underlined;
        final Object other$underlined = other.underlined;
        if (this$underlined == null ? other$underlined != null : !this$underlined.equals(other$underlined))
            return false;
        final Object this$strikethrough = this.strikethrough;
        final Object other$strikethrough = other.strikethrough;
        if (this$strikethrough == null ? other$strikethrough != null : !this$strikethrough.equals(other$strikethrough))
            return false;
        final Object this$obfuscated = this.obfuscated;
        final Object other$obfuscated = other.obfuscated;
        if (this$obfuscated == null ? other$obfuscated != null : !this$obfuscated.equals(other$obfuscated))
            return false;
        final Object this$insertion = this.insertion;
        final Object other$insertion = other.insertion;
        if (this$insertion == null ? other$insertion != null : !this$insertion.equals(other$insertion)) return false;
        final Object this$extra = this.extra;
        final Object other$extra = other.extra;
        if (this$extra == null ? other$extra != null : !this$extra.equals(other$extra)) return false;
        final Object this$clickEvent = this.clickEvent;
        final Object other$clickEvent = other.clickEvent;
        if (this$clickEvent == null ? other$clickEvent != null : !this$clickEvent.equals(other$clickEvent))
            return false;
        final Object this$hoverEvent = this.hoverEvent;
        final Object other$hoverEvent = other.hoverEvent;
        return this$hoverEvent == null ? other$hoverEvent == null : this$hoverEvent.equals(other$hoverEvent);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BaseComponent;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $color = this.getColor();
        result = result * PRIME + ($color == null ? 43 : $color.hashCode());
        final Object $font = this.getFont();
        result = result * PRIME + ($font == null ? 43 : $font.hashCode());
        final Object $bold = this.bold;
        result = result * PRIME + ($bold == null ? 43 : $bold.hashCode());
        final Object $italic = this.italic;
        result = result * PRIME + ($italic == null ? 43 : $italic.hashCode());
        final Object $underlined = this.underlined;
        result = result * PRIME + ($underlined == null ? 43 : $underlined.hashCode());
        final Object $strikethrough = this.strikethrough;
        result = result * PRIME + ($strikethrough == null ? 43 : $strikethrough.hashCode());
        final Object $obfuscated = this.obfuscated;
        result = result * PRIME + ($obfuscated == null ? 43 : $obfuscated.hashCode());
        final Object $insertion = this.insertion;
        result = result * PRIME + ($insertion == null ? 43 : $insertion.hashCode());
        final Object $extra = this.extra;
        result = result * PRIME + ($extra == null ? 43 : $extra.hashCode());
        final Object $clickEvent = this.clickEvent;
        result = result * PRIME + ($clickEvent == null ? 43 : $clickEvent.hashCode());
        final Object $hoverEvent = this.hoverEvent;
        result = result * PRIME + ($hoverEvent == null ? 43 : $hoverEvent.hashCode());
        return result;
    }

    public String toString() {
        return "BaseComponent(color=" + this.getColor() + ", font=" + this.getFont() + ", bold=" + this.bold + ", italic=" + this.italic + ", underlined=" + this.underlined + ", strikethrough=" + this.strikethrough + ", obfuscated=" + this.obfuscated + ", insertion=" + this.insertion + ", extra=" + this.extra + ", clickEvent=" + this.clickEvent + ", hoverEvent=" + this.hoverEvent + ")";
    }

    public String getInsertion() {
        return this.insertion;
    }

    public List<BaseComponent> getExtra() {
        return this.extra;
    }

    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    public HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    public void setParent(BaseComponent parent) {
        this.parent = parent;
    }
}
