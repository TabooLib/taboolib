package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;
import taboolib.common.util.Vector;

/**
 * 坐标工具类
 *
 * @author Zoyn
 */
@Isolated
public class LocationUtils {

    /**
     * 在二维平面上利用给定的中心点逆时针旋转一个点
     *
     * @param location 待旋转的点
     * @param angle    旋转角度
     * @param point    中心点
     * @return {@link Location}
     */
    public static Location rotateLocationAboutPoint(Location location, double angle, Location point) {
        double radians = Math.toRadians(angle);
        double dx = location.getX() - point.getX();
        double dz = location.getZ() - point.getZ();

        double newX = dx * Math.cos(radians) - dz * Math.sin(radians) + point.getX();
        double newZ = dz * Math.cos(radians) + dx * Math.sin(radians) + point.getZ();
        return new Location(location.getWorld(), newX, location.getY(), newZ);
    }

    /**
     * 将一个点围绕另一个向量旋转
     *
     * @param location 给定的点
     * @param origin   向量起始点
     * @param angle    旋转角度
     * @param axis     旋转轴
     * @return {@link Location}
     */
    public static Location rotateLocationAboutVector(Location location, Location origin, double angle, Vector axis) {
        Vector vector = location.clone().subtract(origin).toVector();
        return origin.clone().add(VectorUtils.rotateAroundAxis(vector, axis, angle));
    }
}
