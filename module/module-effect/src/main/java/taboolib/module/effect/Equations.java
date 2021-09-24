package taboolib.module.effect;

import taboolib.common.Isolated;

import java.util.function.Function;

@Isolated
public class Equations {

    /**
     * 二次函数
     */
    public static final Function<Double, Double> QUADRATIC_FUNCTION = x -> Math.pow(x, 2);

    /**
     * 一次函数
     */
    public static final Function<Double, Double> LINEAR_FUNCTION = x -> x;

    /**
     * cos函数
     */
    public static final Function<Double, Double> COS_FUNCTION = Math::cos;

    /**
     * sin函数
     */
    public static final Function<Double, Double> SIN_FUNCTION = Math::sin;

    /**
     * 极坐标:四叶玫瑰线
     */
    public static final Function<Double, Double> POLAR_FOUR_LEAVE_CURVE = theta -> 1.5 * Math.sin(2 * theta);

}
