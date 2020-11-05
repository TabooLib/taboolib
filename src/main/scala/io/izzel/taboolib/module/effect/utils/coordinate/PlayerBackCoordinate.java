package io.izzel.taboolib.module.effect.utils.coordinate;

import io.izzel.taboolib.module.effect.utils.LocationUtils;
import org.bukkit.Location;

/**
 * 将玩家背后转换为一个平面直角坐标系
 *
 * @author Zoyn
 */
public class PlayerBackCoordinate {

    private final Location originDot;
    private final double rotateAngle;

    public PlayerBackCoordinate(Location playerLocation) {
        // 旋转的角度
        rotateAngle = playerLocation.getYaw();
        originDot = playerLocation.clone();
        // 重设仰俯角
        originDot.setPitch(0);
        // 使原点与玩家有一点点距离
        originDot.add(originDot.getDirection().multiply(-0.3));
    }

    public Location newLocation(double x, double y, double z) {
        return LocationUtils.rotateLocationAboutPoint(originDot.clone().add(-x, y, z), rotateAngle, originDot);
    }

}
