package me.skymc.taboolib.inventory;

import me.skymc.taboolib.other.NumberUtils;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class DropUtils {
	
	public static Item drop(Player player, ItemStack itemStack, double bulletSpread, double radius) {
		Location location = player.getLocation();
		location.setY(location.getY() + 1.5);
		
		Item item = player.getWorld().dropItem(location, itemStack);
		
        double yaw = Math.toRadians(-player.getLocation().getYaw() - 90.0F);
        double pitch = Math.toRadians(-player.getLocation().getPitch());
        
        double x = 0;
        double y = 0;
        double z = 0;
        
        if (bulletSpread > 0) {
        	double[] spread = { 1.0D, 1.0D, 1.0D };
            for (int t = 0; t < 3; t++) {
            	spread[t] = ((NumberUtils.getRand().nextDouble() - NumberUtils.getRand().nextDouble()) * bulletSpread * 0.1D);
            }
            
            x = Math.cos(pitch) * Math.cos(yaw) + spread[0];
            y = Math.sin(pitch) + spread[1];
            z = -Math.sin(yaw) * Math.cos(pitch) + spread[2];
        }
        else {
        	x = Math.cos(pitch) * Math.cos(yaw);
            y = Math.sin(pitch);
            z = -Math.sin(yaw) * Math.cos(pitch);
        }
        Vector dirVel = new Vector(x, y, z);
        dirVel.normalize().multiply(radius);
		
        item.setVelocity(dirVel);
        return item;
	}

}
