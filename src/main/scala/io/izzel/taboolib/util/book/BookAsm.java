package io.izzel.taboolib.util.book;

import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.util.chat.BaseComponent;
import org.bukkit.inventory.meta.BookMeta;

/**
 * @Author sky
 * @Since 2019-08-18 16:44
 */
public abstract class BookAsm {

    @TInject(asm = "io.izzel.taboolib.util.book.BookAsmImpl")
    private static BookAsm handle;

    public static BookAsm getHandle() {
        return handle;
    }

    abstract public void setPages(BookMeta bookmeta, BaseComponent[]... pages);

    abstract public void addPages(BookMeta bookmeta, BaseComponent[]... pages);

}
