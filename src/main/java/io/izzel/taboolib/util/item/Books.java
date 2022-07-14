package io.izzel.taboolib.util.item;

import com.cryptomorin.xseries.XMaterial;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.tellraw.TellrawJson;
import io.izzel.taboolib.util.book.BookAsm;
import io.izzel.taboolib.util.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * 书本编辑工具
 *
 * @author sky
 * @since 2020-10-02 03:19
 */
@SuppressWarnings("ConstantConditions")
public class Books {

    private final ItemStack itemStack;
    private final BookMeta itemMeta;

    public Books(XMaterial material) {
        this.itemStack = material.parseItem();
        this.itemMeta = (BookMeta) itemStack.getItemMeta();
        this.itemMeta.setTitle("Book");
        this.itemMeta.setAuthor("TabooLib");
    }

    public Books write(String raw) {
        itemMeta.addPage(raw);
        return this;
    }

    public Books write(TellrawJson json) {
        int i = itemMeta.getPageCount();
        BookAsm.getHandle().addPages(itemMeta, ComponentSerializer.parse(json.toRawMessage()));
        if (i == itemMeta.getPageCount()) {
            itemMeta.addPage(json.toRawMessage());
        }
        return this;
    }

    public Books write(Consumer<TellrawJson> json) {
        TellrawJson tellrawJson = TellrawJson.create();
        json.accept(tellrawJson);
        return write(tellrawJson);
    }

    public Books title(String name) {
        itemMeta.setTitle(name);
        return this;
    }

    public Books author(String author) {
        itemMeta.setAuthor(author);
        return this;
    }

    public Books open(Player player) {
        openBook(player, build());
        return this;
    }

    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static Books create() {
        return new Books(XMaterial.WRITTEN_BOOK);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public BookMeta getItemMeta() {
        return itemMeta;
    }

    /**
     * 为玩家打开一本书
     *
     * @param player 玩家
     * @param book   书本物品实例
     */
    public static void openBook(Player player, ItemStack book) {
        ItemStack hand = player.getItemInHand();
        player.setItemInHand(book);
        try {
            NMS.handle().openBook(player, book);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        player.setItemInHand(hand);
    }

    /**
     * 为玩家打开一本书
     *
     * @param player 玩家
     * @param lines  文本内容
     */
    public static void openBook(Player player, List<String> lines) {
        openBook(player, toBook(lines));
    }

    /**
     * 将多行文本转换为一本书，自动换页但不换行
     *
     * @param lines 文本内容
     * @return 书本物品实例
     */
    @NotNull
    public static ItemStack toBook(List<String> lines) {
        Books book = Books.create();
        TellrawJson json = null;
        int index = 0;
        for (String line : lines) {
            if (json == null) {
                json = TellrawJson.create();
            }
            json.append(line).newLine();
            if (index++ == 13) {
                book.write(json);
                json = null;
                index = 0;
            }
        }
        if (json != null) {
            book.write(json);
        }
        return book.build();
    }
}
