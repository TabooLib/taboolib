package taboolib.module.effect;

import taboolib.common.Isolated;
import taboolib.common.util.Location;
import taboolib.common.util.Vector;

import java.util.function.BiFunction;

/**
 * 表示一个二维至三维投影器
 * <p>算法由 @Bryan33 提供</p>
 *
 * @author Zoyn
 * @since 2020/9/19
 */
@Isolated
public class TwoDProjector {

    private final Location origin;
    private final Vector n1;
    private final Vector n2;

    /**
     * @param origin 投影的原点
     * @param n      投影屏幕的法向量
     */
    public TwoDProjector(Location origin, Vector n) {
        this.origin = origin;
        Vector t = n.clone();
        t.setY(t.getY() + 1);
        this.n1 = n.clone().crossProduct(t).normalize();
        this.n2 = this.n1.clone().crossProduct(n).normalize();
    }

    /**
     * 创建二维至三维投影器
     * 此方法返回的是BiFunction, 可以不用直接调用构造器
     *
     * @param loc 投影的原点
     * @param n   投影屏幕的法向量
     * @return {@link BiFunction}
     */
    public static BiFunction<Double, Double, Location> create2DProjector(Location loc, Vector n) {
        Vector t = n.clone();
        t.setY(t.getY() + 1);
        Vector n1 = n.clone().crossProduct(t).normalize();
        Vector n2 = n1.clone().crossProduct(n).normalize();
        return (x, y) -> {
            Vector r = n1.clone().multiply(x).add(n2.clone().multiply(y));
            return loc.clone().add(r);
        };
    }

    public Location apply(double x, double y) {
        Vector r = n1.clone().multiply(x).add(n2.clone().multiply(y));
        return origin.clone().add(r);
    }
}
