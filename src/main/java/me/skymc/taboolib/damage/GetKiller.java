package me.skymc.taboolib.damage;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

@Deprecated
public class GetKiller {

    public static Player get(EntityDeathEvent e) {
        return e.getEntity().getKiller();
    }

}
