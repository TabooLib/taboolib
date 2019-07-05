//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.izzel.taboolib.util.chat;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author md_5
 */
public class TextComponent extends BaseComponent {

    private static final Pattern url = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
    private String text;

    public static BaseComponent[] fromLegacyText(String message) {
        ArrayList<BaseComponent> components = new ArrayList();
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        Matcher matcher = url.matcher(message);

        for(int i = 0; i < message.length(); ++i) {
            char c = message.charAt(i);
            TextComponent old;
            if (c == 167) {
                ++i;
                c = message.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    c = (char)(c + 32);
                }

                ChatColor format = ChatColor.getByChar(c);
                if (format != null) {
                    if (builder.length() > 0) {
                        old = component;
                        component = new TextComponent(component);
                        old.setText(builder.toString());
                        builder = new StringBuilder();
                        components.add(old);
                    }

                    switch(format) {
                        case BOLD:
                            component.setBold(true);
                            break;
                        case ITALIC:
                            component.setItalic(true);
                            break;
                        case UNDERLINE:
                            component.setUnderlined(true);
                            break;
                        case STRIKETHROUGH:
                            component.setStrikethrough(true);
                            break;
                        case MAGIC:
                            component.setObfuscated(true);
                            break;
                        case RESET:
                            format = ChatColor.WHITE;
                        default:
                            component = new TextComponent();
                            component.setColor(format);
                            break;
                    }
                }
            } else {
                int pos = message.indexOf(32, i);
                if (pos == -1) {
                    pos = message.length();
                }

                if (matcher.region(i, pos).find()) {
                    if (builder.length() > 0) {
                        old = component;
                        component = new TextComponent(component);
                        old.setText(builder.toString());
                        builder = new StringBuilder();
                        components.add(old);
                    }

                    old = component;
                    component = new TextComponent(component);
                    String urlString = message.substring(i, pos);
                    component.setText(urlString);
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, urlString.startsWith("http") ? urlString : "http://" + urlString));
                    components.add(component);
                    i += pos - i - 1;
                    component = old;
                } else {
                    builder.append(c);
                }
            }
        }

        if (builder.length() > 0) {
            component.setText(builder.toString());
            components.add(component);
        }

        if (components.isEmpty()) {
            components.add(new TextComponent(""));
        }

        return components.toArray(new BaseComponent[0]);
    }

    public TextComponent() {
        this.text = "";
    }

    public TextComponent(TextComponent textComponent) {
        super(textComponent);
        this.setText(textComponent.getText());
    }

    public TextComponent(BaseComponent... extras) {
        this.setText("");
        this.setExtra(new ArrayList(Arrays.asList(extras)));
    }

    @Override
    public BaseComponent duplicate() {
        return new TextComponent(this);
    }

    @Override
    protected void toPlainText(StringBuilder builder) {
        builder.append(this.text);
        super.toPlainText(builder);
    }

    @Override
    protected void toLegacyText(StringBuilder builder) {
        builder.append(this.getColor());
        if (this.isBold()) {
            builder.append(ChatColor.BOLD);
        }

        if (this.isItalic()) {
            builder.append(ChatColor.ITALIC);
        }

        if (this.isUnderlined()) {
            builder.append(ChatColor.UNDERLINE);
        }

        if (this.isStrikethrough()) {
            builder.append(ChatColor.STRIKETHROUGH);
        }

        if (this.isObfuscated()) {
            builder.append(ChatColor.MAGIC);
        }

        builder.append(this.text);
        super.toLegacyText(builder);
    }

    @Override
    public String toString() {
        return String.format("TextComponent{text=%s, %s}", this.text, super.toString());
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @ConstructorProperties({"text"})
    public TextComponent(String text) {
        this.text = text;
    }
}
