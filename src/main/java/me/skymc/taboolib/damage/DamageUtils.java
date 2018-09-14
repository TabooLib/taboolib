package me.skymc.taboolib.damage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * @author sky
 */
public class DamageUtils {

    public static Player getAttackerInDamageEvent(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            return (Player) e.getDamager();
        } else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player) {
            return (Player) ((Projectile) e.getDamager()).getShooter();
        } else {
            return null;
        }
    }

    // *********************************
    //
    //           Deprecated
    //
    // *********************************

    @Deprecated
    public static void damage(Player player, LivingEntity victim, double damage) {
        dmg(player, victim, damage);
    }

    @Deprecated
    public static void damage(Player player, Entity victim, double damage) {
        dmg(player, (LivingEntity) victim, damage);
    }

    @Deprecated
    public static void dmg(LivingEntity attacker, LivingEntity victim, double damage) {
        attacker.damage(damage, victim);
    }
}
