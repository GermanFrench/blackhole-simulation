package blackhole.physics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GravityPhysics}.
 */
class GravityPhysicsTest {

    private static final double DELTA = 1e-8;

    /** Black hole at origin for all tests. */
    private BlackHole bhAtOrigin() {
        return new BlackHole(Vector2D.ZERO, BlackHole.DEFAULT_MASS_KG);
    }

    @Test
    void testAccelerationPointsTowardsBlackHole() {
        BlackHole bh = bhAtOrigin();
        // Particle directly to the right of the black hole
        Particle p = new Particle(new Vector2D(100, 0), Vector2D.ZERO, 0);
        Vector2D acc = GravityPhysics.computeAcceleration(p, bh);

        // Acceleration must point in the −x direction (towards origin)
        assertTrue(acc.x < 0, "Acceleration x should be negative (towards BH)");
        assertEquals(0.0, acc.y, DELTA, "Acceleration y should be zero (aligned)");
    }

    @Test
    void testAccelerationMagnitudeDecreasesWith1OverRSquared() {
        BlackHole bh = bhAtOrigin();
        Particle p1  = new Particle(new Vector2D(100, 0), Vector2D.ZERO, 0);
        Particle p2  = new Particle(new Vector2D(200, 0), Vector2D.ZERO, 0);

        double a1 = GravityPhysics.computeAcceleration(p1, bh).magnitude();
        double a2 = GravityPhysics.computeAcceleration(p2, bh).magnitude();

        // a ∝ 1/r²  →  a1/a2 ≈ (r2/r1)² = 4  (with small softening correction)
        double ratio = a1 / a2;
        // Should be close to 4.0, allowing for softening (ε=1 px, r=100/200 px)
        assertEquals(4.0, ratio, 0.005);
    }

    @Test
    void testEventHorizonDetection_inside() {
        BlackHole bh = bhAtOrigin();
        double eh = bh.getEventHorizonPixels();
        // Place particle just inside the event horizon
        Particle p = new Particle(new Vector2D(eh * 0.9, 0), Vector2D.ZERO, 0);
        assertTrue(GravityPhysics.isInsideEventHorizon(p, bh));
    }

    @Test
    void testEventHorizonDetection_outside() {
        BlackHole bh = bhAtOrigin();
        double eh = bh.getEventHorizonPixels();
        // Place particle just outside the event horizon
        Particle p = new Particle(new Vector2D(eh * 1.1, 0), Vector2D.ZERO, 0);
        assertFalse(GravityPhysics.isInsideEventHorizon(p, bh));
    }

    @Test
    void testCircularOrbitalSpeed() {
        BlackHole bh = bhAtOrigin();
        double r = 200.0; // px
        double v = GravityPhysics.circularOrbitalSpeed(bh, r);
        // v = sqrt(μ/r)
        double expected = Math.sqrt(bh.getGravParam() / r);
        assertEquals(expected, v, expected * DELTA);
    }

    @Test
    void testSymmetry() {
        BlackHole bh = new BlackHole(new Vector2D(400, 300));
        // Two particles at equal distances but opposite sides should feel
        // equal-magnitude (but opposite direction) accelerations.
        Particle p1 = new Particle(new Vector2D(400 + 150, 300), Vector2D.ZERO, 0);
        Particle p2 = new Particle(new Vector2D(400 - 150, 300), Vector2D.ZERO, 0);

        Vector2D a1 = GravityPhysics.computeAcceleration(p1, bh);
        Vector2D a2 = GravityPhysics.computeAcceleration(p2, bh);

        assertEquals(a1.magnitude(), a2.magnitude(), DELTA);
        // Directions should be opposite
        assertEquals(a1.x, -a2.x, DELTA);
        assertEquals(a1.y, a2.y,  DELTA);
    }
}
