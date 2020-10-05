package io.izzel.taboolib.module.tellraw.internal;

import io.izzel.taboolib.module.tellraw.TellrawVersion;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author 坏黑
 * @Since 2018-11-07 22:52
 */
public interface AbstractTellraw {

    void sendRawMessage(Player player, String rawMessage);

    String getItemComponent(ItemStack itemStack);

    String getItemComponent(ItemStack itemStack, TellrawVersion version);

    ItemStack optimizeNBT(ItemStack itemStack, List<String> nbtWhitelist);

    ItemStack optimizeShulkerBox(ItemStack itemStack);

}
