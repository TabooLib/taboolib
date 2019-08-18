package io.izzel.taboolib.util.book;

import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.util.book.builder.BookBuilder;
import io.izzel.taboolib.util.book.builder.PageBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author unknown
 * @recode 2019-8-18 16:40:16
 */
public class BookFormatter {

    public static void forceOpen(Player player, ItemStack book) {
        ItemStack hand = player.getItemInHand();
        player.setItemInHand(book);
        try {
            NMS.handle().openBook(player, book);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        player.setItemInHand(hand);
    }

    public static BookBuilder writtenBook() {
        return new BookBuilder(new ItemStack(Material.WRITTEN_BOOK));
    }

    public static BookBuilder writtenBook(String title, String author) {
        return new BookBuilder(new ItemStack(Material.WRITTEN_BOOK), title, author);
    }

    public static ItemStack writeToBook(List<String> lines) {
        BookBuilder bookBuilder = BookFormatter.writtenBook();
        PageBuilder builder = null;
        int index = 0;
        for (String line : lines) {
            if (builder == null) {
                builder = new PageBuilder();
            }
            builder.add(line).newLine();
            if (index++ == 13) {
                bookBuilder.addPages(builder.build());
                builder = null;
                index = 0;
            }
        }
        if (builder != null) {
            bookBuilder.addPages(builder.build());
        }
        return bookBuilder.build();
    }
}
