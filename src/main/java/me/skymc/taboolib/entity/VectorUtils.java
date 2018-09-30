package me.skymc.taboolib.entity;

import me.skymc.taboolib.other.NumberUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.stream.IntStream;

/**
 * @Author sky
 * @Since 2018-06-24 16:32
 */
public class VectorUtils {

    /**
     * 物品丢弃
     * <p>
     * 常用参数：
     * itemDrop(player, itemStack, 0.2, 0.5)
     *
     * @param player       玩家
     * @param itemStack    丢弃物品
     * @param bulletSpread 视角偏移
     * @param radius       距离
     * @return {@link Item}
     */
    public static Item itemDrop(Player player, ItemStack itemStack, double bulletSpread, double radius) {
        Location location = player.getLocation().add(0, 1.5, 0);
        Item item = player.getWorld().dropItem(location, itemStack);

        double yaw = Math.toRadians(-player.getLocation().getYaw() - 90.0F);
        double pitch = Math.toRadians(-player.getLocation().getPitch());
        double x;
        double y;
        double z;

        if (bulletSpread > 0) {
            double[] spread = {1.0D, 1.0D, 1.0D};
            IntStream.range(0, 3).forEach(t -> spread[t] = ((NumberUtils.getRandom().nextDouble() - NumberUtils.getRandom().nextDouble()) * bulletSpread * 0.1D));
            x = Math.cos(pitch) * Math.cos(yaw) + spread[0];
            y = Math.sin(pitch) + spread[1];
            z = -Math.sin(yaw) * Math.cos(pitch) + spread[2];
        } else {
            x = Math.cos(pitch) * Math.cos(yaw);
            y = Math.sin(pitch);
            z = -Math.sin(yaw) * Math.cos(pitch);
        }

        Vector dirVel = new Vector(x, y, z);
        item.setVelocity(dirVel.normalize().multiply(radius));
        return item;
    }

    /**
     * 生物抛射
     * <p>
     * 常用参数：
     * entityPush(entity, location, 15)
     *
     * @param entity   目标生物
     * @param to       目标坐标
     * @param velocity 力量
     */
    public static void entityPush(Entity entity, Location to, double velocity) {
        Location from = entity.getLocation();

        Vector test = to.clone().subtract(from).toVector();
        double elevation = test.getY();

        Double launchAngle = calculateLaunchAngle(from, to, velocity, elevation, 20.0D);
        double distance = Math.sqrt(Math.pow(test.getX(), 2.0D) + Math.pow(test.getZ(), 2.0D));
        if (distance == 0.0D) {
            return;
        }
        if (launchAngle == null) {
            launchAngle = Math.atan((40.0D * elevation + Math.pow(velocity, 2.0D)) / (40.0D * elevation + 2.0D * Math.pow(velocity, 2.0D)));
        }
        double hangTime = calculateHangTime(launchAngle, velocity, elevation, 20.0D);

        test.setY(Math.tan(launchAngle) * distance);
        test = normalizeVector(test);

        Vector noise = Vector.getRandom();
        noise = noise.multiply(1 / 10.0D);
        test.add(noise);

        velocity = velocity + 1.188D * Math.pow(hangTime, 2.0D) + (NumberUtils.getRandom().nextDouble() - 0.8D) / 2.0D;
        test = test.multiply(velocity / 20.0D);

        entity.setVelocity(test);
    }

    // *********************************
    //
    //        Private Methods
    //
    // *********************************

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
