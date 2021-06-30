package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;

/**
 * 表示一个将X轴显示在玩家面前的坐标器
 * <p>自动修正在XZ平面上的粒子朝向</p>
 *
 * @author Zoyn
 */
@Isolated
public class PlayerFixedCoordinate {

    /**
     * 原点
     */
    private final Location originDot;
    /**
     * 旋转角度
     */
    private final double rotateAngle;

    public PlayerFixedCoordinate(Location playerLocation) {
        // 旋转的角度
        rotateAngle = playerLocation.getYaw();
        originDot = playerLocation.clone();
        // 重设仰俯角, 防止出现仰头后旋转角度不正确的问题
        originDot.setPitch(0);
    }

    public Location getOriginDot() {
        return originDot;
    }

    public Location newLocation(double x, double y, double z) {
        return LocationUtils.rotateLocationAboutPoint(originDot.clone().add(-x, y, z), rotateAngle, originDot);
    }
}