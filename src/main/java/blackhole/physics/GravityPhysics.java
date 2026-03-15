package blackhole.physics;

/**
 * Computes gravitational interactions between a {@link Particle} and a
 * {@link BlackHole}.
 *
 * <h2>Gravitational Acceleration</h2>
 * <p>Newton's law of gravitation gives the force on a test mass {@code m}
 * at distance {@code r} from a body of mass {@code M}:</p>
 *
 * <pre>
 *   F = (G · M · m) / r²
 * </pre>
 *
 * <p>Dividing by {@code m}, the resulting acceleration is:</p>
 *
 * <pre>
 *   a = (G · M) / r²  =  μ / r²
 * </pre>
 *
 * <p>where {@code μ = G·M} is the standard gravitational parameter.  In the
 * simulation this parameter is stored as
 * {@link BlackHole#getGravParam()}, already scaled to pixel units (px³/s²).
 * The acceleration vector points from the particle towards the black hole.</p>
 *
 * <h2>Softening</h2>
 * <p>To avoid numerical divergence when a particle is very close to the
 * singularity, a small softening length {@link #SOFTENING_PX} is added under
 * the square root:</p>
 *
 * <pre>
 *   a = μ / (r² + ε²)
 * </pre>
 *
 * <p>Particles that come within the event horizon are detected by
 * {@link #isInsideEventHorizon(Particle, BlackHole)} and should be removed by
 * the simulation engine; the softening term is therefore only a safety net
 * for the integrator, not a physical model.</p>
 */
public final class GravityPhysics {

    /**
     * Softening length in pixels.
     * Prevents the denominator from reaching zero near the singularity.
     */
    public static final double SOFTENING_PX = 1.0;

    // Utility class – no instances needed.
    private GravityPhysics() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Computes the gravitational acceleration vector experienced by
     * {@code particle} due to {@code blackHole}.
     *
     * <pre>
     *   a = μ / (r² + ε²)  ·  r̂
     * </pre>
     *
     * @param particle  the test particle
     * @param blackHole the gravitational source
     * @return acceleration vector in px/s², pointing towards the black hole
     */
    public static Vector2D computeAcceleration(Particle particle, BlackHole blackHole) {
        // Direction vector from particle to black hole
        Vector2D delta = blackHole.getPosition().subtract(particle.getPosition());

        double distSq = delta.magnitudeSquared();

        // Softened inverse-square law:  a = μ / (r² + ε²)
        double accelMagnitude = blackHole.getGravParam()
                / (distSq + SOFTENING_PX * SOFTENING_PX);

        // The acceleration direction is the unit vector towards the black hole
        return delta.normalize().scale(accelMagnitude);
    }

    /**
     * Returns the gravitational acceleration given only a position delta
     * and gravitational parameter.  Used internally by the RK4 integrator
     * to evaluate intermediate stages without a full {@link Particle} object.
     *
     * @param delta     vector from evaluation point to black hole centre
     * @param gravParam μ = G·M in px³/s²
     * @return acceleration vector in px/s²
     */
    public static Vector2D computeAccelerationFromDelta(Vector2D delta, double gravParam) {
        double distSq = delta.magnitudeSquared();
        double accelMagnitude = gravParam / (distSq + SOFTENING_PX * SOFTENING_PX);
        return delta.normalize().scale(accelMagnitude);
    }

    /**
     * Returns {@code true} when the particle's distance to the black hole
     * centre is less than or equal to the event horizon radius.
     *
     * @param particle  the particle to test
     * @param blackHole the black hole
     * @return {@code true} if the particle is inside (or on) the event horizon
     */
    public static boolean isInsideEventHorizon(Particle particle, BlackHole blackHole) {
        double distSq = particle.getPosition()
                                .subtract(blackHole.getPosition())
                                .magnitudeSquared();
        double eh = blackHole.getEventHorizonPixels();
        return distSq <= eh * eh;
    }

    /**
     * Computes the circular orbital speed at a given distance from the black hole.
     *
     * <pre>
     *   v_circ = √(μ / r)
     * </pre>
     *
     * @param blackHole        the black hole
     * @param orbitalRadiusPx  orbital radius in pixels
     * @return orbital speed in px/s (tangential)
     */
    public static double circularOrbitalSpeed(BlackHole blackHole, double orbitalRadiusPx) {
        return Math.sqrt(blackHole.getGravParam() / orbitalRadiusPx);
    }
}
