package io.izzel.taboolib.util.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author md_5
 */
public final class TranslatableComponent extends BaseComponent {

    private final ResourceBundle locales = ResourceBundle.getBundle("mojang-translations/en_US");
    private final Pattern format = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    /**
     * The key into the Minecraft locale files to use for the translation. The
     * text depends on the client's locale setting. The console is always en_US
     */
    private String translate;
    /**
     * The components to substitute into the translation
     */
    private List<BaseComponent> with;

    public ResourceBundle getLocales() {
        return locales;
    }

    public Pattern getFormat() {
        return format;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    public List<BaseComponent> getWith() {
        return with;
    }

    public TranslatableComponent() {
    }

    /**
     * Creates a translatable component from the original to clone it.
     *
     * @param original the original for the new translatable component.
     */
    public TranslatableComponent(TranslatableComponent original) {
        super(original);
        setTranslate(original.getTranslate());

        if (original.getWith() != null) {
            setWith(original.getWith().stream().map(BaseComponent::duplicate).collect(Collectors.toList()));
        }
    }

    /**
     * Creates a translatable component with the passed substitutions
     *
     * @param translate the translation key
     * @param with      the {@link String}s and
     *                  {@link BaseComponent}s to use into the
     *                  translation
     * @see #translate
     * @see #setWith(List)
     */
    public TranslatableComponent(String translate, Object... with) {
        setTranslate(translate);
        if (with != null && with.length != 0) {
            List<BaseComponent> temp = new ArrayList<>();
            for (Object w : with) {
                if (w instanceof String) {
                    temp.add(new TextComponent((String) w));
                } else {
                    temp.add((BaseComponent) w);
                }
            }
            setWith(temp);
        }
    }

    /**
     * Creates a duplicate of this TranslatableComponent.
     *
     * @return the duplicate of this TranslatableComponent.
     */
    @Override
    public BaseComponent duplicate() {
        return new TranslatableComponent(this);
    }

    @Override
    public String toString() {
        return "locales=" + "TranslatableComponent{" + locales + ", format=" + format + ", translate='" + translate + '\'' + ", with=" + with + '}';
    }

    /**
     * Sets the translation substitutions to be used in this component. Removes
     * any previously set substitutions
     *
     * @param components the components to substitute
     */
    public void setWith(List<BaseComponent> components) {
        components.forEach(component -> component.parent = this);
        with = components;
    }

    /**
     * Adds a text substitution to the component. The text will inherit this
     * component's formatting
     *
     * @param text the text to substitute
     */
    public void addWith(String text) {
        addWith(new TextComponent(text));
    }

    /**
     * Adds a component substitution to the component. The text will inherit
     * this component's formatting
     *
     * @param component the component to substitute
     */
    public void addWith(BaseComponent component) {
        if (with == null) {
            with = new ArrayList<>();
        }
        component.parent = this;
        with.add(component);
    }

    @Override
    protected void toPlainText(StringBuilder builder) {
        String trans;
        try {
            trans = locales.getString(translate);
        } catch (MissingResourceException ex) {
            trans = translate;
        }

        Matcher matcher = format.matcher(trans);
        int position = 0;
        int i = 0;
        while (matcher.find(position)) {
            int pos = matcher.start();
            if (pos != position) {
                builder.append(trans, position, pos);
            }
            position = matcher.end();

            String formatCode = matcher.group(2);
            switch (formatCode.charAt(0)) {
                case 's':
                case 'd':
                    String withIndex = matcher.group(1);
                    with.get(withIndex != null ? Integer.parseInt(withIndex) - 1 : i++).toPlainText(builder);
                    break;
                case '%':
                    builder.append('%');
                    break;
                default:
                    break;
            }
        }
        if (trans.length() != position) {
            builder.append(trans, position, trans.length());
        }
        super.toPlainText(builder);
    }

    @Override
    protected void toLegacyText(StringBuilder builder) {
        String trans;
        try {
            trans = locales.getString(translate);
        } catch (MissingResourceException e) {
            trans = translate;
        }

        Matcher matcher = format.matcher(trans);
        int position = 0;
        int i = 0;
        while (matcher.find(position)) {
            int pos = matcher.start();
            if (pos != position) {
                addFormat(builder);
                builder.append(trans, position, pos);
            }
            position = matcher.end();

            String formatCode = matcher.group(2);
            switch (formatCode.charAt(0)) {
                case 's':
                case 'd':
                    String withIndex = matcher.group(1);
                    with.get(withIndex != null ? Integer.parseInt(withIndex) - 1 : i++).toLegacyText(builder);
                    break;
                case '%':
                    addFormat(builder);
                    builder.append('%');
                    break;
                default:
                    break;
            }
        }
        if (trans.length() != position) {
            addFormat(builder);
            builder.append(trans, position, trans.length());
        }
        super.toLegacyText(builder);
    }

    private void addFormat(StringBuilder builder) {
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
}
