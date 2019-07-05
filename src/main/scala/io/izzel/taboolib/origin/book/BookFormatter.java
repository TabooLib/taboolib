package io.izzel.taboolib.origin.book;

import io.izzel.taboolib.origin.book.builder.BookBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public final class BookFormatter {

    /**
     * Opens a book GUI to the player
     * @param p the player
     * @param book the book to be opened
     */
    public static void forceOpen(Player p, ItemStack book) {
    	//Close inventory currently
        p.closeInventory();
        //Store the previous item
        ItemStack hand = p.getItemInHand();
        p.setItemInHand(book);

        //Opening the GUI
        BookReflection.openBook(p, book, false);

        //Returning whatever was on hand.
        p.setItemInHand(hand);
    }

    /**
     * Creates a BookBuilder instance with a written book as the Itemstack's type
     * @return
     */
    public static BookBuilder writtenBook() {
        return new BookBuilder(new ItemStack(Material.WRITTEN_BOOK));
    }
    
    /**
     * Creates a BookBuilder instance with a written book as the Itemstack's type
     * @return
     */
    public static BookBuilder writtenBook(String title, String author) {
        return new BookBuilder(new ItemStack(Material.WRITTEN_BOOK), title, author);
    }
}
