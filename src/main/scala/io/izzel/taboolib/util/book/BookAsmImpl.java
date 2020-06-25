package io.izzel.taboolib.util.book;

import io.izzel.taboolib.Version;
import io.izzel.taboolib.util.chat.BaseComponent;
import io.izzel.taboolib.util.chat.ComponentSerializer;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftMetaBook;
import org.bukkit.inventory.meta.BookMeta;

/**
 * @Author sky
 * @Since 2019-08-18 16:46
 */
public class BookAsmImpl extends BookAsm {

    private final boolean v11600 = Version.isAfter(Version.v1_16);

    @Override
    public void setPages(BookMeta bookmeta, BaseComponent[]... pages) {
        ((CraftMetaBook) bookmeta).pages.clear();
        addPages(bookmeta, pages);
    }

    @Override
    public void addPages(BookMeta bookmeta, BaseComponent[]... pages) {
        for (BaseComponent[] components : pages) {
            if (v11600) {
                ((org.bukkit.craftbukkit.v1_16_R1.inventory.CraftMetaBook) bookmeta).pages.add(net.minecraft.server.v1_16_R1.IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(components)));
            } else {
                ((CraftMetaBook) bookmeta).pages.add(IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(components)));
            }
        }
    }
}
