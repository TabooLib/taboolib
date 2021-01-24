package io.izzel.taboolib.module.db.local.player;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author sky
 * @since 2020-07-03 18:19
 */
public abstract class LocalPlayerHandler {

    abstract public void save();

    abstract public void save(OfflinePlayer player);

    abstract public FileConfiguration get(OfflinePlayer player);

    abstract public FileConfiguration get0(OfflinePlayer player);

    abstract public void set0(OfflinePlayer player, FileConfiguration file);

}
