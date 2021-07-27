package taboolib.module.effect;

import taboolib.common.util.Location;
import taboolib.common.util.Vector;

import java.util.Arrays;

/**
 * 表示一个 m*n 的矩阵
 * <p>在该类中, 所有的乘法操作都是左乘</p>
 *
 * @author Zoyn
 */
public class Matrix {

    private final double[][] m;

    public Matrix(int row, int column) {
        this.m = new double[row][column];
    }

    public Matrix(double[][] m) {
        this.m = m;
    }

    public Matrix(Matrix matrix) {
        this.m = matrix.getAsArray();
    }

    public int getRow() {
        return m.length;
    }

    public int getColumn() {
        return m[0].length;
    }

    public double[][] getAsArray() {
        return m;
    }

    public double get(int row, int column) {
        return m[row - 1][column - 1];
    }

    /**
     * 通过给定的值设定矩阵内对应的数值
     *
     * @param row    行数
     * @param column 列数
     * @param value  数值
     * @return {@link Matrix}
     */
    public Matrix set(int row, int column, double value) {
        m[row - 1][column - 1] = value;
        return this;
    }

    /**
     * 填充矩阵的某一行为同一实数
     *
     * @param row   行数
     * @param value 实数
     * @return {@link Matrix}
     */
    public Matrix fill(int row, double value) {
        Arrays.fill(m[row - 1], value);
        return this;
    }

    /**
     * 取出矩阵中单独的一行
     *
     * @param row 行数
     * @return 对应行所成的数组
     */
    public double[] getRowAsArray(int row) {
        return Arrays.copyOf(m[row - 1], getColumn());
    }

    /**
     * 取矩阵中单独的一列
     *
     * @param column 列数
     * @return 列所成的数组
     */
    public double[] getColumnAsArray(int column) {
        double[] m = new double[getRow()];
        for (int row = 0; row < getRow(); row++) {
            m[row] = get(row + 1, column);
        }
        return m;
    }

    public boolean isSameRow(Matrix matrix) {
        return getRow() == matrix.getRow();
    }

    public boolean isSameColumn(Matrix matrix) {
        return getColumn() == matrix.getColumn();
    }

    public boolean isSameRowAndColumn(Matrix matrix) {
        return isSameRow(matrix) && isSameColumn(matrix);
    }

    /**
     * 将该矩阵进行转置变换
     *
     * @return {@link Matrix}
     */
    public Matrix invert() {
        double[][] n = new double[getColumn()][getRow()];
        for (int i = 0; i < getRow(); i++) {
            for (int j = 0; j < getColumn(); j++) {
                n[j][i] = m[i][j];
            }
        }
        return new Matrix(n);
    }

    /**
     * 将两个矩阵相加
     * <p>注意: 本矩阵的大小要等于另一矩阵的大小</p>
     *
     * @param matrix 给定的矩阵
     * @return {@link Matrix}
     */
    public Matrix plus(Matrix matrix) {
        if (!isSameRowAndColumn(matrix)) {
            throw new IllegalArgumentException("两矩阵大小不相同!");
        }

        double[][] n = matrix.getAsArray();
        double[][] result = new double[getRow()][getColumn()];
        for (int row = 0; row < getRow(); row++) {
            for (int column = 0; column < getColumn(); column++) {
                result[row][column] = m[row][column] + n[row][column];
            }
        }
//        this.m = result;
        return new Matrix(result);
    }

    /**
     * 将该矩阵乘以一个实数
     *
     * @param value 给定的数
     * @return {@link Matrix}
     */
    public Matrix multiply(double value) {
        double[][] result = new double[getRow()][getColumn()];

        for (int row = 0; row < getRow(); row++) {
            for (int column = 0; column < getColumn(); column++) {
                result[row][column] = m[row][column] * value;
            }
        }
        return new Matrix(result);
    }

    /**
     * 将该矩阵乘以另一个矩阵
     * <p>注意: 本矩阵的列数要等于另外一个矩阵的行数</p>
     *
     * @param matrix 给定的另一矩阵
     * @return {@link Matrix}
     */
    public Matrix multiply(Matrix matrix) {
        if (getColumn() != matrix.getRow()) {
            throw new IllegalArgumentException("原矩阵的列数不等于新矩阵的行数");
        }

        double[][] n = matrix.getAsArray();
        double[][] result = new double[getRow()][matrix.getColumn()];

        for (int row = 0; row < getRow(); row++) {
            for (int column = 0; column < matrix.getColumn(); column++) {
                double[] x = getRowAsArray(row + 1);
                double[] y = matrix.getColumnAsArray(column + 1);
                for (int i = 0; i < x.length; i++) {
                    result[row][column] += x[i] * y[i];
                }
            }
        }

        return new Matrix(result);
    }

    /**
     * 将矩阵漂亮的打印出来
     */
    public void prettyPrinting() {
        for (double[] doubles : m) {
            System.out.println(Arrays.toString(doubles));
        }
    }

    /**
     * 将本矩阵的变换作用至给定的坐标上
     *
     * @param location 给定的坐标
     * @return {@link Location}
     */
    public Location applyLocation(Location location) {
        if (getRow() == 2 && getColumn() == 2) {
            return applyIn2DLocation(location);
        } else if (getRow() == 3 && getColumn() == 3) {
            return applyIn3DLocation(location);
        }

        throw new IllegalArgumentException("当前矩阵非 2*2 或 3*3 的方阵");
    }

    /**
     * 将本矩阵的变换作用至给定的向量上
     *
     * @param vector 给定的向量
     * @return {@link Location}
     */
    public Vector applyVector(Vector vector) {
        if (getRow() == 2 && getColumn() == 2) {
            return applyIn2DVector(vector);
        } else if (getRow() == 3 && getColumn() == 3) {
            return applyIn3DVector(vector);
        }

        throw new IllegalArgumentException("当前矩阵非 2*2 或 3*3 的方阵");
    }

    private Vector applyIn2DVector(Vector vector) {
        double x = vector.getX();
        double z = vector.getZ();
        double ax = getAsArray()[0][0] * x;
        double ay = getAsArray()[0][1] * z;

        double bx = getAsArray()[1][0] * x;
        double by = getAsArray()[1][1] * z;

        return new Vector(ax + ay, vector.getY(), bx + by);
    }

    private Vector applyIn3DVector(Vector vector) {
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();

        double ax = getAsArray()[0][0] * x;
        double ay = getAsArray()[0][1] * y;
        double az = getAsArray()[0][2] * z;

        double bx = getAsArray()[1][0] * x;
        double by = getAsArray()[1][1] * y;
        double bz = getAsArray()[1][2] * z;

        double cx = getAsArray()[2][0] * x;
        double cy = getAsArray()[2][1] * y;
        double cz = getAsArray()[2][2] * z;

        return new Vector(ax + ay + az, bx + by + bz, cx + cy + cz);
    }

    private Location applyIn2DLocation(Location location) {
        double x = location.getX();
        double z = location.getZ();
        double ax = getAsArray()[0][0] * x;
        double ay = getAsArray()[0][1] * z;

        double bx = getAsArray()[1][0] * x;
        double by = getAsArray()[1][1] * z;

        return new Location(location.getWorld(), ax + ay, location.getY(), bx + by, location.getYaw(), location.getPitch());
    }

    private Location applyIn3DLocation(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        double ax = getAsArray()[0][0] * x;
        double ay = getAsArray()[0][1] * y;
        double az = getAsArray()[0][2] * z;

        double bx = getAsArray()[1][0] * x;
        double by = getAsArray()[1][1] * y;
        double bz = getAsArray()[1][2] * z;

        double cx = getAsArray()[2][0] * x;
        double cy = getAsArray()[2][1] * y;
        double cz = getAsArray()[2][2] * z;

        return new Location(location.getWorld(), ax + ay + az, bx + by + bz, cx + cy + cz, location.getYaw(), location.getPitch());
    }

}
