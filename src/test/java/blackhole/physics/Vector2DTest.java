package blackhole.physics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Vector2D}.
 */
class Vector2DTest {

    private static final double DELTA = 1e-10;

    @Test
    void testAdd() {
        Vector2D a = new Vector2D(1, 2);
        Vector2D b = new Vector2D(3, 4);
        Vector2D result = a.add(b);
        assertEquals(4.0, result.x, DELTA);
        assertEquals(6.0, result.y, DELTA);
    }

    @Test
    void testSubtract() {
        Vector2D a = new Vector2D(5, 7);
        Vector2D b = new Vector2D(2, 3);
        Vector2D result = a.subtract(b);
        assertEquals(3.0, result.x, DELTA);
        assertEquals(4.0, result.y, DELTA);
    }

    @Test
    void testScale() {
        Vector2D v = new Vector2D(3, 4);
        Vector2D scaled = v.scale(2.0);
        assertEquals(6.0, scaled.x, DELTA);
        assertEquals(8.0, scaled.y, DELTA);
    }

    @Test
    void testMagnitude() {
        Vector2D v = new Vector2D(3, 4);
        assertEquals(5.0, v.magnitude(), DELTA);
    }

    @Test
    void testMagnitudeSquared() {
        Vector2D v = new Vector2D(3, 4);
        assertEquals(25.0, v.magnitudeSquared(), DELTA);
    }

    @Test
    void testNormalize() {
        Vector2D v = new Vector2D(3, 4);
        Vector2D unit = v.normalize();
        assertEquals(1.0, unit.magnitude(), DELTA);
        assertEquals(0.6, unit.x, DELTA);
        assertEquals(0.8, unit.y, DELTA);
    }

    @Test
    void testNormalizeZeroVector() {
        Vector2D zero = Vector2D.ZERO;
        Vector2D result = zero.normalize();
        assertEquals(Vector2D.ZERO, result);
    }

    @Test
    void testDot() {
        Vector2D a = new Vector2D(1, 0);
        Vector2D b = new Vector2D(0, 1);
        assertEquals(0.0, a.dot(b), DELTA); // perpendicular

        Vector2D c = new Vector2D(2, 3);
        Vector2D d = new Vector2D(4, 5);
        assertEquals(23.0, c.dot(d), DELTA); // 2*4 + 3*5
    }

    @Test
    void testDistanceTo() {
        Vector2D a = new Vector2D(0, 0);
        Vector2D b = new Vector2D(3, 4);
        assertEquals(5.0, a.distanceTo(b), DELTA);
    }

    @Test
    void testFromPolar() {
        Vector2D v = Vector2D.fromPolar(0, 5); // angle=0 → (5, 0)
        assertEquals(5.0, v.x, DELTA);
        assertEquals(0.0, v.y, DELTA);

        Vector2D v2 = Vector2D.fromPolar(Math.PI / 2, 1); // angle=90° → (0, 1)
        assertEquals(0.0, v2.x, DELTA);
        assertEquals(1.0, v2.y, DELTA);
    }

    @Test
    void testImmutability() {
        Vector2D original = new Vector2D(1, 2);
        original.add(new Vector2D(10, 20));
        // original should be unchanged
        assertEquals(1.0, original.x, DELTA);
        assertEquals(2.0, original.y, DELTA);
    }
}
