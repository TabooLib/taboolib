package taboolib.module.effect;

import taboolib.common.Isolated;

/**
 * 与 {@link Matrix} 相关的静态实用方法
 *
 * @author Zoyn
 */
@Isolated
public class Matrixs {

    /**
     * 通过给定的行列数返回一个全零矩阵
     *
     * @param row    行数
     * @param column 列数
     * @return {@link Matrix}
     */
    public static Matrix zeros(int row, int column) {
        return new Matrix(row, column);
    }

    /**
     * 通过给定的行列数返回一个全一矩阵
     *
     * @param row    行数
     * @param column 列数
     * @return {@link Matrix}
     */
    public static Matrix ones(int row, int column) {
        Matrix matrix = new Matrix(row, column);
        for (int i = 0; i < row; i++) {
            matrix.fill(i + 1, 1);
        }
        return matrix;
    }

    /**
     * 通过给定的行列数返回一个单位矩阵
     *
     * @param row    行数
     * @param column 列数
     * @return {@link Matrix}
     */
    public static Matrix eyes(int row, int column) {
        double[][] result = new double[row][column];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                // 行列相等时则为 1
                if (i == j) {
                    result[i][j] = 1;
                    continue;
                }
                result[i][j] = 0;
            }
        }
        return new Matrix(result);
    }

    /**
     * 通过给定的角度返回一个平面旋转矩阵
     *
     * @param theta 旋转角度
     * @return {@link Matrix}
     */
    public static Matrix rotate2D(double theta) {
        double[][] m = new double[2][2];
        double radians = Math.toRadians(-theta);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        m[0][0] = cos;
        m[0][1] = -sin;
        m[1][0] = sin;
        m[1][1] = cos;
        return new Matrix(m);
    }

    /**
     * 通过给定的角度返回一个关于X轴的旋转矩阵
     * <p>注意：该方法会返回3阶方阵</p>
     *
     * @param theta 旋转角度
     * @return {@link Matrix}
     */
    public static Matrix rotateAroundXAxis(double theta) {
        Matrix matrix = eyes(3, 3);

        double radians = Math.toRadians(-theta);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        matrix.set(2, 2, cos)
                .set(2, 3, sin)
                .set(3, 2, -sin)
                .set(3, 3, cos);
        return matrix;
    }

    /**
     * 通过给定的角度返回一个关于Z轴的旋转矩阵
     * <p>注意：该方法会返回3阶方阵</p>
     *
     * @param theta 旋转角度
     * @return {@link Matrix}
     */
    public static Matrix rotateAroundYAxis(double theta) {
        Matrix matrix = eyes(3, 3);

        double radians = Math.toRadians(-theta);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        matrix.set(1, 1, cos)
                .set(1, 3, -sin)
                .set(3, 1, sin)
                .set(3, 3, cos);
        return matrix;
    }

    /**
     * 通过给定的角度返回一个关于Z轴的旋转矩阵
     * <p>注意：该方法会返回3阶方阵</p>
     *
     * @param theta 旋转角度
     * @return {@link Matrix}
     */
    public static Matrix rotateAroundZAxis(double theta) {
        Matrix matrix = eyes(3, 3);

        double radians = Math.toRadians(-theta);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        matrix.set(1, 1, cos)
                .set(1, 2, sin)
                .set(2, 1, -sin)
                .set(2, 2, cos);
        return matrix;
    }

    /**
     * 建立一个放大或缩小的矩阵
     *
     * @param row    行数
     * @param column 列数
     * @param value  放大或缩小的值
     * @return {@link Matrix}
     */
    public static Matrix scale(int row, int column, double value) {
        return eyes(row, column).multiply(value);
    }

}
