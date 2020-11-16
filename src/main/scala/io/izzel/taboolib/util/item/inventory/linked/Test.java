package io.izzel.taboolib.util.item.inventory.linked;

import io.izzel.taboolib.util.item.inventory.ClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * TabooLib
 * io.izzel.taboolib.util.item.inventory.linked.Test
 *
 * @author bkm016
 * @since 2020/11/17 1:41 上午
 */
public class Test extends MenuLinked<Player> {

    @Override
    public List<Player> getElements() {
        return null;
    }

    @Override
    public List<Integer> getSlots() {
        return null;
    }

    @Override
    public void onBuild(@NotNull Inventory inventory) {

    }

    @Override
    public void onClick(@NotNull ClickEvent event, @NotNull Player element) {

    }
}
