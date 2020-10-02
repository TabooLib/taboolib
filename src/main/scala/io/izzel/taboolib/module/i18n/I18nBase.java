package io.izzel.taboolib.module.i18n;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @Author sky
 * @Since 2020-04-04 19:42
 */
public abstract class I18nBase {

    abstract public void init();

    abstract public String getName(Player player, Entity entity);

    abstract public String getName(Player player, ItemStack itemStack);

    public String getName(Entity entity) {
        return getName(null, entity);
    }

    public String getName(ItemStack itemStack) {
        return getName(null, itemStack);
    }
}
