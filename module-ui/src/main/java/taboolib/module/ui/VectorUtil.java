package taboolib.module.ui;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.stream.IntStream;

public class VectorUtil {

    public static Item itemDrop(Player player, ItemStack itemStack) {
        return itemDrop(player, itemStack, 0.0, 0.4);
    }

    public static Item itemDrop(Player player, ItemStack itemStack, double bulletSpread, double radius) {
        Location location = player.getLocation().add(0.0D, 1.5D, 0.0D);
        Item item = player.getWorld().dropItem(location, itemStack);
        double yaw = Math.toRadians((double)(-player.getLocation().getYaw() - 90.0F));
        double pitch = Math.toRadians((double)(-player.getLocation().getPitch()));
        double x;
        double y;
        double z;
        double v = Math.cos(pitch) * Math.cos(yaw);
        double v1 = -Math.sin(yaw) * Math.cos(pitch);
        if (bulletSpread > 0.0D) {
            double[] spread = new double[]{1.0D, 1.0D, 1.0D};
            IntStream.range(0, 3).forEach((t) -> {
                spread[t] = (new Random().nextDouble() - new Random().nextDouble()) * bulletSpread * 0.1D;
            });
            x = v + spread[0];
            y = Math.sin(pitch) + spread[1];
            z = v1 + spread[2];
        } else {
            x = v;
            y = Math.sin(pitch);
            z = v1;
        }
        Vector dirVel = new Vector(x, y, z);
        item.setVelocity(dirVel.normalize().multiply(radius));
        return item;
    }

    public static void entityPush(Entity entity, Location to, double velocity) {
        Location from = entity.getLocation();
        Vector test = to.clone().subtract(from).toVector();
        double elevation = test.getY();
        Double launchAngle = calculateLaunchAngle(from, to, velocity, elevation, 20.0D);
        double distance = Math.sqrt(Math.pow(test.getX(), 2.0D) + Math.pow(test.getZ(), 2.0D));
        if (distance != 0.0D) {
            if (launchAngle == null) {
                launchAngle = Math.atan((40.0D * elevation + Math.pow(velocity, 2.0D)) / (40.0D * elevation + 2.0D * Math.pow(velocity, 2.0D)));
            }
            double hangTime = calculateHangTime(launchAngle, velocity, elevation, 20.0D);
            test.setY(Math.tan(launchAngle) * distance);
            test = normalizeVector(test);
            Vector noise = Vector.getRandom();
            noise = noise.multiply(0.1D);
            test.add(noise);
            velocity = velocity + 1.188D * Math.pow(hangTime, 2.0D) + (new Random().nextDouble() - 0.8D) / 2.0D;
            test = test.multiply(velocity / 20.0D);
            entity.setVelocity(test);
        }
    }

    private static double calculateHangTime(double launchAngle, double v, double elev, double g) {
        double a = v * Math.sin(launchAngle);
        double b = -2.0D * g * elev;
        return Math.pow(a, 2.0D) + b < 0.0D ? 0.0D : (a + Math.sqrt(Math.pow(a, 2.0D) + b)) / g;
    }

    private static Vector normalizeVector(Vector victor) {
        double mag = Math.sqrt(Math.pow(victor.getX(), 2.0D) + Math.pow(victor.getY(), 2.0D) + Math.pow(victor.getZ(), 2.0D));
        return mag != 0.0D ? victor.multiply(1.0D / mag) : victor.multiply(0);
    }

    private static Double calculateLaunchAngle(Location from, Location to, double v, double elevation, double g) {
        Vector vector = from.clone().subtract(to).toVector();
        double distance = Math.sqrt(Math.pow(vector.getX(), 2.0D) + Math.pow(vector.getZ(), 2.0D));
        double v2 = Math.pow(v, 2.0D);
        double v4 = Math.pow(v, 4.0D);
        double check = g * (g * Math.pow(distance, 2.0D) + 2.0D * elevation * v2);
        return v4 < check ? null : Math.atan((v2 - Math.sqrt(v4 - check)) / (g * distance));
    }
}
