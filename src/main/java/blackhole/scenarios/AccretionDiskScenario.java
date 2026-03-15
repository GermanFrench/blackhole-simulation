package blackhole.scenarios;

import blackhole.physics.BlackHole;
import blackhole.physics.GravityPhysics;
import blackhole.physics.Particle;
import blackhole.physics.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Spawns a ring of particles in quasi-circular orbits that together form a
 * visible accretion disk around the black hole.
 *
 * <h2>Physics</h2>
 * <p>For a circular orbit at radius {@code r} the required tangential speed is:</p>
 * <pre>
 *   v_circ = √(μ / r)
 * </pre>
 * <p>where {@code μ = G·M} is the black hole's gravitational parameter.
 * Each particle's velocity is set to {@code v_circ} (with a small random
 * perturbation so orbits differ slightly), directed perpendicular to the
 * radius vector.</p>
 *
 * <p>Particles are spread over an annular band between
 * {@link #INNER_RADIUS_FACTOR} and {@link #OUTER_RADIUS_FACTOR} times the
 * event horizon radius, seeded at evenly spaced azimuthal angles.</p>
 */
public final class AccretionDiskScenario {

    /** Inner edge of the disk, expressed as a multiple of the event horizon radius. */
    public static final double INNER_RADIUS_FACTOR = 2.5;

    /** Outer edge of the disk, expressed as a multiple of the event horizon radius. */
    public static final double OUTER_RADIUS_FACTOR = 7.0;

    /** Fractional velocity perturbation applied to each particle (±). */
    private static final double VELOCITY_SCATTER = 0.08;

    /** Random seed for reproducible scenario initialisation. */
    private static final long SEED = 42L;

    // Utility class.
    private AccretionDiskScenario() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Creates a set of particles arranged in an accretion disk.
     *
     * <p>Particles are spread across {@code rings} concentric rings,
     * each with {@code particlesPerRing} evenly spaced azimuthal positions.
     * A small random inclination and velocity scatter are applied so
     * consecutive rings look organic rather than perfectly symmetric.</p>
     *
     * @param blackHole       the black hole at the centre
     * @param rings           number of concentric rings
     * @param particlesPerRing number of particles per ring
     * @return list of initialised particles ready to be added to the engine
     */
    public static List<Particle> create(BlackHole blackHole,
                                        int rings,
                                        int particlesPerRing) {
        List<Particle> particles = new ArrayList<>();
        Random rng = new Random(SEED);

        double eh          = blackHole.getEventHorizonPixels();
        double innerRadius = eh * INNER_RADIUS_FACTOR;
        double outerRadius = eh * OUTER_RADIUS_FACTOR;

        for (int ring = 0; ring < rings; ring++) {
            // Interpolate radius linearly across rings
            double t      = rings == 1 ? 0.5 : (double) ring / (rings - 1);
            double radius = innerRadius + t * (outerRadius - innerRadius);

            // Hue shifts from orange (inner, hot) to blue-white (outer, cooler)
            double hue = 20.0 + t * 200.0;

            // Circular orbital speed at this radius
            double vCirc = GravityPhysics.circularOrbitalSpeed(blackHole, radius);

            // Azimuthal offset per ring so they don't start aligned
            double azimuthOffset = rng.nextDouble() * 2 * Math.PI;

            for (int j = 0; j < particlesPerRing; j++) {
                double angle = azimuthOffset + (2.0 * Math.PI * j) / particlesPerRing;

                // Position on the ring
                Vector2D offset = Vector2D.fromPolar(angle, radius);
                Vector2D pos    = blackHole.getPosition().add(offset);

                // Tangential velocity with a small scatter to make orbits elliptical
                double scatter = 1.0 + (rng.nextDouble() * 2 - 1) * VELOCITY_SCATTER;
                double speed   = vCirc * scatter;

                // Perpendicular direction (90° CCW relative to radial)
                Vector2D tangent = new Vector2D(-Math.sin(angle), Math.cos(angle));
                Vector2D vel     = tangent.scale(speed);

                particles.add(new Particle(pos, vel, hue));
            }
        }

        return particles;
    }

    /**
     * Convenience overload that creates a standard 5-ring disk with 20
     * particles per ring (100 particles total).
     *
     * @param blackHole the black hole at the centre
     * @return list of initialised particles
     */
    public static List<Particle> createDefault(BlackHole blackHole) {
        return create(blackHole, 5, 20);
    }
}
