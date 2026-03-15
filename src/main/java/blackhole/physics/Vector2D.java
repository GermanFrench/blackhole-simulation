package blackhole.physics;

/**
 * Immutable 2-D vector used throughout the physics engine.
 *
 * <p>All operations return a new {@code Vector2D} so that objects can be
 * treated as value types, avoiding accidental mutation of shared state.</p>
 */
public final class Vector2D {

    /** X component. */
    public final double x;
    /** Y component. */
    public final double y;

    /** Zero vector constant. */
    public static final Vector2D ZERO = new Vector2D(0, 0);

    /**
     * Constructs a vector with the given components.
     *
     * @param x the x component
     * @param y the y component
     */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // -----------------------------------------------------------------------
    // Arithmetic
    // -----------------------------------------------------------------------

    /** Returns {@code this + other}. */
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    /** Returns {@code this - other}. */
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }

    /** Returns {@code this * scalar}. */
    public Vector2D scale(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    /** Returns {@code this / scalar}. */
    public Vector2D divide(double scalar) {
        return new Vector2D(x / scalar, y / scalar);
    }

    // -----------------------------------------------------------------------
    // Geometry
    // -----------------------------------------------------------------------

    /** Returns the Euclidean length of this vector. */
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    /** Returns the squared length (avoids a costly sqrt when only comparison is needed). */
    public double magnitudeSquared() {
        return x * x + y * y;
    }

    /**
     * Returns a unit vector in the same direction.
     * Returns {@link #ZERO} if this vector has zero length.
     */
    public Vector2D normalize() {
        double mag = magnitude();
        if (mag == 0) {
            return ZERO;
        }
        return divide(mag);
    }

    /** Returns the dot product of this vector with {@code other}. */
    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }

    /** Returns the distance from this position to {@code other}. */
    public double distanceTo(Vector2D other) {
        return subtract(other).magnitude();
    }

    // -----------------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------------

    /** Creates a vector from a polar angle (radians) and magnitude. */
    public static Vector2D fromPolar(double angle, double magnitude) {
        return new Vector2D(Math.cos(angle) * magnitude, Math.sin(angle) * magnitude);
    }

    /** Returns a perpendicular (90-degree counter-clockwise rotated) unit vector. */
    public Vector2D perpendicular() {
        return new Vector2D(-y, x).normalize();
    }

    @Override
    public String toString() {
        return String.format("Vector2D(%.4f, %.4f)", x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector2D v)) return false;
        return Double.compare(x, v.x) == 0 && Double.compare(y, v.y) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(x) + Double.hashCode(y);
    }
}
