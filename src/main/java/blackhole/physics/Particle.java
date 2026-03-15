package blackhole.physics;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A massless test particle whose trajectory is affected by the black hole's
 * gravitational field.
 *
 * <p>Each particle stores:
 * <ul>
 *   <li>Current position and velocity in pixel coordinates.</li>
 *   <li>Current acceleration (recalculated each physics step).</li>
 *   <li>A fixed-length trail of recent positions used by the renderer to
 *       draw the particle's trajectory.</li>
 *   <li>An {@code absorbed} flag that is set when the particle crosses the
 *       event horizon, signalling the engine to remove it from the
 *       simulation.</li>
 * </ul>
 * </p>
 */
public class Particle {

    // -----------------------------------------------------------------------
    // Rendering constants
    // -----------------------------------------------------------------------

    /** Maximum number of past positions stored in the trail. */
    public static final int MAX_TRAIL_LENGTH = 60;

    /** Visual radius of the particle dot, in pixels. */
    public static final double DISPLAY_RADIUS = 3.0;

    // -----------------------------------------------------------------------
    // Physics state
    // -----------------------------------------------------------------------

    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration;

    // -----------------------------------------------------------------------
    // Visual state
    // -----------------------------------------------------------------------

    /** Circular buffer of past positions for trail rendering. */
    private final Deque<Vector2D> trail = new ArrayDeque<>(MAX_TRAIL_LENGTH + 1);

    /** Colour hue in [0, 360) used by the renderer. */
    private final double hue;

    /** Flag set when the particle crosses the event horizon. */
    private boolean absorbed;

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    /**
     * Creates a particle at the given position with an initial velocity.
     *
     * @param position initial position in pixel coordinates
     * @param velocity initial velocity in px/s
     * @param hue      colour hue in [0, 360) for rendering
     */
    public Particle(Vector2D position, Vector2D velocity, double hue) {
        this.position     = position;
        this.velocity     = velocity;
        this.acceleration = Vector2D.ZERO;
        this.hue          = hue;
        this.absorbed     = false;
    }

    // -----------------------------------------------------------------------
    // Accessors and mutators
    // -----------------------------------------------------------------------

    /** Returns the current position in pixel coordinates. */
    public Vector2D getPosition() {
        return position;
    }

    /** Sets the current position and appends it to the trail buffer. */
    public void setPosition(Vector2D position) {
        // Record old position in trail before updating
        trail.addLast(this.position);
        if (trail.size() > MAX_TRAIL_LENGTH) {
            trail.pollFirst();
        }
        this.position = position;
    }

    /** Returns the current velocity in px/s. */
    public Vector2D getVelocity() {
        return velocity;
    }

    /** Sets the current velocity. */
    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    /** Returns the most recently computed acceleration in px/s². */
    public Vector2D getAcceleration() {
        return acceleration;
    }

    /** Sets the acceleration (called by the physics engine each step). */
    public void setAcceleration(Vector2D acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * Returns an unmodifiable view of the position trail.
     * Index 0 is the oldest recorded position.
     */
    public Deque<Vector2D> getTrail() {
        return trail;
    }

    /** Returns the colour hue in [0, 360) assigned at construction. */
    public double getHue() {
        return hue;
    }

    /** Returns {@code true} if this particle has been absorbed by the black hole. */
    public boolean isAbsorbed() {
        return absorbed;
    }

    /**
     * Marks this particle as absorbed.
     * Called by the simulation engine when the particle crosses the event horizon.
     */
    public void absorb() {
        this.absorbed = true;
    }

    /** Clears the position trail (used when resetting the simulation). */
    public void clearTrail() {
        trail.clear();
    }

    @Override
    public String toString() {
        return String.format("Particle[pos=%s, vel=%s, absorbed=%b]",
                position, velocity, absorbed);
    }
}
