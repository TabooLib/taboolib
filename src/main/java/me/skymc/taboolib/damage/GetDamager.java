package me.skymc.taboolib.damage;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class GetDamager {
	
	public static Player get(EntityDamageByEntityEvent e) {
		Player p = null;
		if (e.getDamager() instanceof Projectile) {
			Projectile arrow = (Projectile) e.getDamager();
			if (arrow.getShooter() instanceof Player) {
				p = (Player) arrow.getShooter();
			}
		}
		else if (e.getDamager() instanceof Player) {
			p = (Player) e.getDamager();
		}
		return p;
	}

}
