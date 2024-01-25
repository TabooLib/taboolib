package taboolib.common5;

import java.util.Objects;

public class Quat {

    public static final double DBL_EPSILON = Double.longBitsToDouble(0x3cb0000000000000L);

    private final double x;
    private final double y;
    private final double z;
    private final double w;

    public Quat(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public double w() {
        return w;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return x * x + y * y + z * z + w * w;
    }

    public Quat rotate(Quat vector) {
        return rotate(vector.x(), vector.y(), vector.z());
    }

    public Quat rotate(double x, double y, double z) {
        final double length = length();
        if (Math.abs(length) < DBL_EPSILON) {
            throw new ArithmeticException("Cannot rotate by zero");
        }
        final double nx = this.x / length;
        final double ny = this.y / length;
        final double nz = this.z / length;
        final double nw = this.w / length;
        final double px = nw * x + ny * z - nz * y;
        final double py = nw * y + nz * x - nx * z;
        final double pz = nw * z + nx * y - ny * x;
        final double pw = -nx * x - ny * y - nz * z;
        return new Quat(
                pw * -nx + px * nw - py * nz + pz * ny,
                pw * -ny + py * nw - pz * nx + px * nz,
                pw * -nz + pz * nw - px * ny + py * nx,
                0);
    }

    public Quat rotate2D(double angle, double aboutX, double aboutZ) {
        return rotate2D(angle, aboutX, aboutZ, 0, 0);
    }

    public Quat rotate2D(double angle, double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = this.x - aboutX;
        double z = this.z - aboutZ;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x2 = x * cos - z * sin;
        double z2 = x * sin + z * cos;
        return new Quat(x2 + aboutX + translateX, this.y, z2 + aboutZ + translateZ, 0);
    }

    public Quat getMinimum(Quat v2) {
        return Quat.at(Math.min(this.x, v2.x), Math.min(this.y, v2.y), Math.min(this.z, v2.z));
    }

    public Quat getMaximum(Quat v2) {
        return Quat.at(Math.max(this.x, v2.x), Math.max(this.y, v2.y), Math.max(this.z, v2.z));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quat)) return false;
        Quat quat = (Quat) o;
        return Double.compare(quat.x, x) == 0 && Double.compare(quat.y, y) == 0 && Double.compare(quat.z, z) == 0 && Double.compare(quat.w, w) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, w);
    }

    @Override
    public String toString() {
        return "Quat{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", w=" + w +
                '}';
    }

    public static Quat one() {
        return Quat.at(1, 1, 1);
    }

    public static Quat zero() {
        return Quat.at(0, 0, 0);
    }

    public static Quat at(double x, double y, double z) {
        return new Quat(x, y, z, 0);
    }

    public static Quat at(double x, double y, double z, double w) {
        return new Quat(x, y, z, w);
    }

    public static Quat radiansAxis(double angle, Quat vector) {
        return radiansAxis(angle, vector.x(), vector.y(), vector.z());
    }

    public static Quat radiansAxis(double angle, double x, double y, double z) {
        final double halfAngle = angle / 2;
        final double q = Math.sin(halfAngle) / Math.sqrt(x * x + y * y + z * z);
        return new Quat(x * q, y * q, z * q, Math.cos(halfAngle));
    }
}
