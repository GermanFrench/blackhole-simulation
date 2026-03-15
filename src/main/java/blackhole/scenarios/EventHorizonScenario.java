package blackhole.scenarios;

import blackhole.physics.BlackHole;
import blackhole.physics.Particle;
import blackhole.physics.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Spawns a fan of particles aimed towards (or near) the black hole to
 * demonstrate event-horizon crossing.
 *
 * <h2>Physics</h2>
 * <p>Particles are launched from a ring at radius
 * {@link #LAUNCH_RADIUS_FACTOR} × r_s with inward radial velocity.  Some
 * particles receive a slight lateral nudge so they follow curved,
 * hyperbolic or parabolic trajectories rather than straight plunge paths.
 * Particles that graze the event horizon are bent and may even escape if
 * they pass outside the photon sphere; those that cross the boundary are
 * absorbed.</p>
 *
 * <p>The launch speed is set to slightly above the local escape velocity
 * from the event horizon (in simulation units):</p>
 * <pre>
 *   v_esc ≈ √(2μ / r)
 * </pre>
 * <p>At the launch radius, a particle aimed straight in will always be
 * captured, while those with lateral velocity may orbit or escape.</p>
 */
public final class EventHorizonScenario {

    /** Launch radius as a multiple of the event horizon radius. */
    public static final double LAUNCH_RADIUS_FACTOR = 8.0;

    /**
     * Fraction of particles given a purely radial (straight-in) trajectory.
     * The remainder receive a random lateral component.
     */
    private static final double RADIAL_FRACTION = 0.4;

    /** Lateral speed range as a fraction of the inward speed. */
    private static final double MAX_LATERAL_FRACTION = 0.6;

    private static final long SEED = 99L;

    // Utility class.
    private EventHorizonScenario() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Creates a set of particles launching towards the black hole from all
     * directions.
     *
     * @param blackHole  the black hole at the centre
     * @param count      total number of particles to spawn
     * @return list of initialised particles
     */
    public static List<Particle> create(BlackHole blackHole, int count) {
        List<Particle> particles = new ArrayList<>(count);
        Random rng = new Random(SEED);

        double eh           = blackHole.getEventHorizonPixels();
        double launchRadius = eh * LAUNCH_RADIUS_FACTOR;
        double mu           = blackHole.getGravParam();

        // Inward speed: slightly above √(2μ/r) (escape velocity at that radius)
        double inwardSpeed = Math.sqrt(2 * mu / launchRadius) * 0.6;

        for (int i = 0; i < count; i++) {
            double angle = 2.0 * Math.PI * i / count;

            // Position on launch ring
            Vector2D offset = Vector2D.fromPolar(angle, launchRadius);
            Vector2D pos    = blackHole.getPosition().add(offset);

            // Inward radial direction (towards black hole)
            Vector2D inward = offset.normalize().scale(-inwardSpeed);

            // Optionally add a tangential component
            Vector2D vel;
            if (rng.nextDouble() < RADIAL_FRACTION) {
                vel = inward; // purely radial plunge
            } else {
                double lateralFrac  = rng.nextDouble() * MAX_LATERAL_FRACTION;
                double lateralSpeed = inwardSpeed * lateralFrac
                        * (rng.nextBoolean() ? 1 : -1);
                Vector2D tangent    = new Vector2D(-Math.sin(angle), Math.cos(angle));
                vel = inward.add(tangent.scale(lateralSpeed));
            }

            // Colour: deep red/magenta spectrum
            double hue = 280.0 + rng.nextDouble() * 100.0;
            particles.add(new Particle(pos, vel, hue % 360));
        }

        return particles;
    }

    /**
     * Convenience overload that creates 36 inward particles (one every 10°).
     *
     * @param blackHole the black hole at the centre
     * @return list of initialised particles
     */
    public static List<Particle> createDefault(BlackHole blackHole) {
        return create(blackHole, 36);
    }
}
