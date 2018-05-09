package me.skymc.taboolib.bookformatter.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author sky
 * @since 2018-03-08 22:36:58
 */
public class PageBuilder {
	
    private List<BaseComponent> text = new ArrayList<>();

    /**
     * Adds a simple black-colored text to the page
     * @param text the text to add
     * @return the PageBuilder's calling instance
     */
    public PageBuilder add(String text) {
        this.text.add(TextBuilder.of(text).build());
        return this;
    }

    /**
     * Adds a component to the page
     * @param component the component to add
     * @return the PageBuilder's calling instance
     */
    public PageBuilder add(BaseComponent component) {
        this.text.add(component);
        return this;
    }

    /**
     * Adds one or more components to the page
     * @param components the components to add
     * @return the PageBuilder's calling instance
     */
    public PageBuilder add(BaseComponent... components) {
        this.text.addAll(Arrays.asList(components));
        return this;
    }

    /**
     * Adds one or more components to the page
     * @param components the components to add
     * @return the PageBuilder's calling instance
     */
    public PageBuilder add(Collection<BaseComponent> components) {
        this.text.addAll(components);
        return this;
    }

    /**
     * Adds a newline to the page (equivalent of adding \n to the previous component)
     * @return the PageBuilder's calling instance
     */
    public PageBuilder newLine() {
        this.text.add(new TextComponent("\n"));
        return this;
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
        return text.toArray(new BaseComponent[0]);
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