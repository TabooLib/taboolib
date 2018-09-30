package me.skymc.taboolib.damage;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@Deprecated
public class GetDamager {

    public static Player get(EntityDamageByEntityEvent e) {
        return DamageUtils.getAttackerInDamageEvent(e);
    }

}
