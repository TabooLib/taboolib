package io.izzel.taboolib.module.inject;

import org.bukkit.entity.Player;

/**
 * TabooLib
 * io.izzel.taboolib.module.inject.Releaseable
 *
 * @author bkm016
 * @since 2020/11/17 1:15 上午
 */
public interface Releasable {

    void release(Player player, String namespace);

}
