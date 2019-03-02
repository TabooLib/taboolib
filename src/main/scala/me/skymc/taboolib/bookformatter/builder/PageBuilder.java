package me.skymc.taboolib.bookformatter.builder;

import com.ilummc.tlib.bungee.api.chat.*;
import me.skymc.taboolib.string.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author sky
 * @since 2018-03-08 22:36:58
 */
public class PageBuilder {
	
    private BaseComponent[] text = TextComponent.fromLegacyText("");

    /**
     * Adds a simple black-colored text to the page
     * @param text the text to add
     * @return the PageBuilder's calling instance
     */
    public PageBuilder add(String text) {
        Arrays.stream(TextComponent.fromLegacyText(text)).forEach(component -> this.text = ArrayUtils.arrayAppend(this.text, component));
        return this;
    }

    /**
     * Adds a component to the page
     * @param component the component to add
     * @return the PageBuilder's calling instance
     */
    public PageBuilder add(BaseComponent component) {
        this.text = ArrayUtils.arrayAppend(this.text, component);
        return this;
    }

    /**
     * Adds one or more components to the page
     * @param components the components to add
     * @return the PageBuilder's calling instance
     */
    public PageBuilder add(BaseComponent... components) {
        Arrays.stream(components).forEach(component -> this.text = ArrayUtils.arrayAppend(this.text, component));
        return this;
    }

    /**
     * Adds a newline to the page (equivalent of adding \n to the previous component)
     * @return the PageBuilder's calling instance
     */
    public PageBuilder newLine() {
        return add("\n");
    }
    
    /**
     * Another way of newLine(), better resolution (equivalent of adding \n to the previous component)
     * @return the PageBuilder's calling instance
     */
    public PageBuilder endLine() {
        return newLine();
    }

    /**
     * Builds the page
     * @return an array of BaseComponents representing the page
     */
    public BaseComponent[] build() {
        return text;
    }

    /**
     * Creates a new PageBuilder instance wih the parameter as the initial text
     * @param text the initial text of the page
     * @return a new PageBuilder with the parameter as the initial text
     */
    public static PageBuilder of(String text) {
        return new PageBuilder().add(text);
    }

    /**
     * Creates a new PageBuilder instance wih the parameter as the initial component
     * @param text the initial component of the page
     * @return a new PageBuilder with the parameter as the initial component
     */
    public static PageBuilder of(BaseComponent text) {
        return new PageBuilder().add(text);
    }

    /**
     * Creates a new PageBuilder instance wih the parameter as the initial components
     * @param text the initial components of the page
     * @return a new PageBuilder with the parameter as the initial components
     */
    public static PageBuilder of(BaseComponent... text) {
        PageBuilder res = new PageBuilder();
        for(BaseComponent b : text) {
            res.add(b);
        }
        return res;
    }
}