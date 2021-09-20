package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;

/**
 * 表示一个玩家面前的坐标系
 * <p>将玩家面前作为一个新坐标系
 * 暂时不会受到仰俯角的控制</p>
 *
 * @author Zoyn
 */
@Isolated
public class PlayerFrontCoordinate {
    /**
     * 原点
     */
    private final Location originDot;
    /**
     * 旋转角度
     */
    private final double rotateAngle;

    public PlayerFrontCoordinate(Location playerLocation) {
        // 旋转的角度
        rotateAngle = playerLocation.getYaw() + 90D;
        originDot = playerLocation.clone();
        // 重设仰俯角, 防止出现仰头后旋转角度不正确的问题
        originDot.setPitch(0);
    }

    public Location getOriginDot() {
        return originDot;
    }

    public Location newLocation(double x, double y, double z) {
        return LocationUtils.rotateLocationAboutPoint(originDot.clone().add(y, z, x), rotateAngle, originDot);
    }

}
