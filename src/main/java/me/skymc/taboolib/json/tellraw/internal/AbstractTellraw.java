package me.skymc.taboolib.json.tellraw.internal;

import me.skymc.taboolib.json.tellraw.TellrawVersion;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @Author 坏黑
 * @Since 2018-11-07 22:52
 */
public interface AbstractTellraw {

    void sendRawMessage(Player player, String rawMessage);

    String getItemComponent(ItemStack itemStack);

    String getItemComponent(ItemStack itemStack, TellrawVersion version);

}
