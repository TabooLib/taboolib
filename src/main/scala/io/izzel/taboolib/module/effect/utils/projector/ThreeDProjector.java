package io.izzel.taboolib.module.effect.utils.projector;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * 表示一个三维至三维投影器
 * <p>算法由 @Bryan33 提供</p>
 *
 * @author Zoyn
 * @since 2020/9/19
 */
public class ThreeDProjector {

    private final Location origin;
    private final Vector n1;
    private final Vector n2;
    private final Vector n3;

    /**
     * @param origin 投影的原点
     * @param n      投影屏幕的法向量
     */
    public ThreeDProjector(Location origin, Vector n) {
        this.origin = origin;
        Vector t = n.clone();
        t.setY(t.getY() + 1);
        this.n1 = n.clone().crossProduct(t).normalize();
        this.n2 = n1.clone().crossProduct(n).normalize();
        this.n3 = n.clone().normalize();
    }

    public Location apply(double x, double y, double z) {
        Vector r = n1.clone().multiply(x).add(n2.clone().multiply(z)).add(n3.clone().multiply(y));
        return origin.clone().add(r);
    }

}
