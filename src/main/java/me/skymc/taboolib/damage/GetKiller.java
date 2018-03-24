package me.skymc.taboolib.damage;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDeathEvent;

public class GetKiller {
	
	public static Player get(EntityDeathEvent e) {
		Player p = null;
		if (e.getEntity().getKiller() instanceof Projectile) {
			Projectile arrow = (Projectile) e.getEntity().getKiller();
			if (arrow.getShooter() instanceof Player) {
				p = (Player) arrow.getShooter();
			}
		}
		else if (e.getEntity().getKiller() instanceof Player) {
			p = e.getEntity().getKiller();
		}
		return p;
	}

}
