package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;

/**
 * 表示一个玩家后背坐标系
 * <p>将玩家背后转换为一个直角坐标系</p>
 *
 * @author Zoyn
 */
@Isolated
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
