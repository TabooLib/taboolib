package me.skymc.taboolib.bookformatter;

import me.skymc.taboolib.bookformatter.builder.BookBuilder;
import me.skymc.taboolib.events.CustomBookOpenEvent;

import org.bukkit.Bukkit;
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
    public static void openPlayer(Player p, ItemStack book) {
        CustomBookOpenEvent event = new CustomBookOpenEvent(p, book, false);
        //Call the CustomBookOpenEvent
        Bukkit.getPluginManager().callEvent(event);
        //Check if it's cancelled
        if(event.isCancelled()) {
            return;
        }
        
        //Close inventory currently
        p.closeInventory();
        //Store the previous item
        ItemStack hand = p.getItemInHand();
        p.setItemInHand(event.getBook());

        //Opening the GUI
        BookReflection.openBook(p, event.getBook(), event.getHand() == CustomBookOpenEvent.Hand.OFF_HAND);

        //Returning whatever was on hand.
        p.setItemInHand(hand);
    }
    
    /**
     * Opens a book GUI to the player, Bypass the {@link CustomBookOpenEvent}
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
