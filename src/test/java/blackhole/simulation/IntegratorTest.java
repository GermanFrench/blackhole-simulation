package blackhole.simulation;

import blackhole.physics.BlackHole;
import blackhole.physics.GravityPhysics;
import blackhole.physics.Particle;
import blackhole.physics.Vector2D;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Integrator}.
 */
class IntegratorTest {

    private static final double DELTA = 1e-6;

    private BlackHole bhAtOrigin() {
        return new BlackHole(Vector2D.ZERO, BlackHole.DEFAULT_MASS_KG);
    }

    @Test
    void testRK4StepMovesParticleTowardsBlackHole() {
        BlackHole bh  = bhAtOrigin();
        // Particle with no initial velocity should fall towards origin
        Particle p = new Particle(new Vector2D(200, 0), Vector2D.ZERO, 0);
        double initialX = p.getPosition().x;

        Integrator.stepRK4(p, bh, SimulationEngine.PHYSICS_DT_SECONDS);

        // x should decrease (particle moves towards BH at origin)
        assertTrue(p.getPosition().x < initialX,
                   "Particle should fall towards black hole");
        // y should remain essentially zero (perfectly aligned)
        assertEquals(0.0, p.getPosition().y, 1e-4);
    }

    @Test
    void testEulerStepMovesParticleTowardsBlackHole() {
        // Symplectic Euler uses updated velocity for position, so a particle
        // starting with zero velocity does move in the first step.
        BlackHole bh = bhAtOrigin();
        Particle p   = new Particle(new Vector2D(200, 0), Vector2D.ZERO, 0);
        double initialX = p.getPosition().x;

        Integrator.stepEuler(p, bh, SimulationEngine.PHYSICS_DT_SECONDS);

        // After one step the updated velocity (pointing left) is used for
        // the position update, so x must decrease.
        assertTrue(p.getPosition().x < initialX,
                   "Symplectic Euler should move particle towards BH in first step");
    }

    @Test
    void testRK4PreservesOrbitBetterThanEuler() {
        // A particle in circular orbit should maintain roughly constant
        // distance from the BH with RK4 (much better than Euler).
        BlackHole bh = bhAtOrigin();
        double r     = 200.0;
        double v     = GravityPhysics.circularOrbitalSpeed(bh, r);

        // Use same initial conditions for both
        Particle rk4  = new Particle(new Vector2D(r, 0), new Vector2D(0, v), 0);
        Particle euler = new Particle(new Vector2D(r, 0), new Vector2D(0, v), 0);

        double dt = SimulationEngine.PHYSICS_DT_SECONDS;
        // Advance for several hundred steps (several seconds of simulation)
        int steps = 600;
        for (int i = 0; i < steps; i++) {
            Integrator.stepRK4(rk4, bh, dt);
            Integrator.stepEuler(euler, bh, dt);
        }

        // RK4 distance error should be smaller than Euler distance error
        double rk4Error   = Math.abs(rk4.getPosition().magnitude() - r);
        double eulerError = Math.abs(euler.getPosition().magnitude() - r);
        assertTrue(rk4Error < eulerError,
                   String.format("RK4 error (%.4f) should be < Euler error (%.4f)",
                                  rk4Error, eulerError));
    }

    @Test
    void testTrailIsPopulatedAfterSteps() {
        BlackHole bh = bhAtOrigin();
        Particle p   = new Particle(new Vector2D(200, 0), new Vector2D(0, 100), 0);
        assertTrue(p.getTrail().isEmpty(), "Trail should start empty");

        Integrator.stepRK4(p, bh, SimulationEngine.PHYSICS_DT_SECONDS);
        assertEquals(1, p.getTrail().size(), "Trail should have 1 entry after first step");

        // Step more times to grow the trail
        for (int i = 0; i < 10; i++) {
            Integrator.stepRK4(p, bh, SimulationEngine.PHYSICS_DT_SECONDS);
        }
        assertEquals(11, p.getTrail().size());
    }

    @Test
    void testAccelerationIsUpdatedAfterStep() {
        BlackHole bh = bhAtOrigin();
        Particle p   = new Particle(new Vector2D(200, 0), Vector2D.ZERO, 0);
        assertEquals(Vector2D.ZERO, p.getAcceleration());

        Integrator.stepRK4(p, bh, SimulationEngine.PHYSICS_DT_SECONDS);
        // Acceleration should now be non-zero and pointing towards origin
        assertNotEquals(Vector2D.ZERO, p.getAcceleration());
        assertTrue(p.getAcceleration().x < 0);
    }
}
