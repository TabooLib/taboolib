package me.skymc.taboolib.bookformatter.builder;

import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.skymc.taboolib.bookformatter.BookReflection;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * @author sky
 * @since 2018-03-08 22:36:14
 */
public class BookBuilder {
	
    private final BookMeta meta;
    private final ItemStack book;

    /**
     * Creates a new instance of the BookBuilder from an ItemStack representing the book item
     * @param book the book's ItemStack
     */
    public BookBuilder(ItemStack book) {
        this.book = book;
        this.meta = (BookMeta)book.getItemMeta();
    }
    
    /**
     * Creates a new instance of the BookBuilder from an ItemStack representing the book item
     * @param book the book's ItemStack
     */
    public BookBuilder(ItemStack book, String title, String author) {
        this.book = book;
        this.meta = (BookMeta)book.getItemMeta();
        this.meta.setTitle(title);
        this.meta.setAuthor(author);
    }

    /**
     * Sets the title of the book
     * @param title the title of the book
     * @return the BookBuilder's calling instance
     */
    public BookBuilder title(String title) {
        meta.setTitle(title);
        return this;
    }

    /**
     * Sets the author of the book
     * @param author the author of the book
     * @return the BookBuilder's calling instance
     */
    public BookBuilder author(String author) {
        meta.setAuthor(author);
        return this;
    }
    
    /**
     * Sets the generation of the book
     * Only works from MC 1.10
     * @param generation the Book generation
     * @return the BookBuilder calling instance
     */
    public BookBuilder generation(BookMeta.Generation generation) {
        meta.setGeneration(generation);
        return this;
    }

    /**
     * Sets the pages of the book without worrying about json or interactivity
     * @param pages text-based pages
     * @return the BookBuilder's calling instance
     */
    public BookBuilder pagesRaw(String... pages) {
        meta.setPages(pages);
        return this;
    }

    /**
     * Sets the pages of the book without worrying about json or interactivity
     * @param pages text-based pages
     * @return the BookBuilder's calling instance
     */
    public BookBuilder pagesRaw(List<String> pages) {
        meta.setPages(pages);
        return this;
    }

    /**
     * Sets the pages of the book
     * @param pages the pages of the book
     * @return the BookBuilder's calling instance
     */
    public BookBuilder pages(BaseComponent[]... pages) {
        BookReflection.setPages(meta, pages);
        return this;
    }

    /**
     * Sets the pages of the book
     * @param pages the pages of the book
     * @return the BookBuilder's calling instance
     */
    public BookBuilder pages(List<BaseComponent[]> pages) {
        BookReflection.setPages(meta, pages.toArray(new BaseComponent[0][]));
        return this;
    }
    
    /**
     * Append the pages of the book
     * @param pages the pages of the book
     * @return the BookBuilder's calling instance
     */
    public BookBuilder addPages(BaseComponent[]... pages) {
    	BookReflection.addPages(meta, pages);
        return this;
    }

    /**
     * Creates the book
     * @return the built book
     */
    public ItemStack build() {
        book.setItemMeta(meta);
        return book;
    }
}