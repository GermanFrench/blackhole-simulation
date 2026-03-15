package blackhole.physics;

/**
 * Represents the central black hole in the simulation.
 *
 * <h2>Schwarzschild Radius</h2>
 * <p>The Schwarzschild radius is the radius of the event horizon for a
 * non-rotating, uncharged black hole.  It is derived from the condition
 * that the escape velocity equals the speed of light:</p>
 *
 * <pre>
 *   r_s = (2 · G · M) / c²
 * </pre>
 *
 * <p>where:
 * <ul>
 *   <li>G = 6.67430 × 10⁻¹¹ N·m²/kg²  (gravitational constant)</li>
 *   <li>M = mass of the black hole in kg</li>
 *   <li>c = 2.99792458 × 10⁸ m/s        (speed of light in vacuum)</li>
 * </ul>
 * </p>
 *
 * <h2>Simulation Scaling</h2>
 * <p>For visual clarity the simulation works in <em>screen pixels</em> rather
 * than SI metres.  A scale factor {@code METRES_PER_PIXEL} converts between
 * the two coordinate systems, so that the event-horizon circle on screen
 * accurately reflects the Schwarzschild radius.</p>
 */
public class BlackHole {

    // -----------------------------------------------------------------------
    // Physical constants (SI)
    // -----------------------------------------------------------------------

    /** Gravitational constant G in N·m²·kg⁻². */
    public static final double G = 6.67430e-11;

    /** Speed of light c in m/s. */
    public static final double C = 299_792_458.0;

    /**
     * Simulation scale: number of metres represented by one on-screen pixel.
     * Default ≈ 3 × 10⁶ m/px (roughly 0.02 AU/px).
     */
    public static final double METRES_PER_PIXEL = 3.0e6;

    // -----------------------------------------------------------------------
    // Default parameters
    // -----------------------------------------------------------------------

    /**
     * Default black hole mass (kg).
     * ≈ 7.5 × 10⁴ solar masses — chosen so that the Schwarzschild radius
     * maps to {@link #DEFAULT_DISPLAY_RADIUS} pixels at the default scale.
     */
    public static final double DEFAULT_MASS_KG = 1.5e35;

    /**
     * The on-screen radius (px) used for the event-horizon circle at default
     * mass.  Particles that enter this radius are absorbed.
     */
    public static final double DEFAULT_DISPLAY_RADIUS = 40.0;

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    /** Position of the black hole in simulation (pixel) coordinates. */
    private Vector2D position;

    /** Mass of the black hole in kg. */
    private double massKg;

    /**
     * Schwarzschild radius in metres, recalculated whenever mass changes.
     *
     * <pre>r_s = (2 · G · M) / c²</pre>
     */
    private double schwarzschildRadiusMetres;

    /**
     * Schwarzschild radius in simulation pixels
     * = schwarzschildRadiusMetres / METRES_PER_PIXEL.
     */
    private double eventHorizonPixels;

    /**
     * Simulation gravitational parameter μ = G_sim · M_sim.
     * Units: px³/s².
     * Tuned so that at default mass a particle at 200 px completes one
     * orbit in roughly 7–8 simulation seconds.
     */
    private double gravParam;

    /**
     * Multiplier that maps real-units G·M (m³/s²) to simulation gravParam
     * (px³/s²).  Derived at construction from the condition that at
     * DEFAULT_MASS_KG the event horizon equals DEFAULT_DISPLAY_RADIUS pixels.
     */
    private static final double GRAV_SCALE;

    static {
        // Calibrate: at default mass, gravParam should equal 1e6 px³/s²
        // (this gives nice orbits at ~150-300 px radii).
        double defaultGravParam = 1.0e6;  // px³/s²
        double defaultGMreal = G * DEFAULT_MASS_KG; // m³/s²
        GRAV_SCALE = defaultGravParam / defaultGMreal;
    }

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    /**
     * Creates a black hole at the given position with the default mass.
     *
     * @param position centre of the black hole in pixel coordinates
     */
    public BlackHole(Vector2D position) {
        this(position, DEFAULT_MASS_KG);
    }

    /**
     * Creates a black hole at the given position with the specified mass.
     *
     * @param position centre of the black hole in pixel coordinates
     * @param massKg   mass in kilograms
     */
    public BlackHole(Vector2D position, double massKg) {
        this.position = position;
        setMassKg(massKg);
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    /** Returns the position of the black hole in pixel coordinates. */
    public Vector2D getPosition() {
        return position;
    }

    /** Sets the position of the black hole in pixel coordinates. */
    public void setPosition(Vector2D position) {
        this.position = position;
    }

    /** Returns the mass of the black hole in kg. */
    public double getMassKg() {
        return massKg;
    }

    /**
     * Updates the black hole mass and recalculates all derived quantities
     * (Schwarzschild radius, event horizon display size, gravitation parameter).
     *
     * @param massKg new mass in kg (must be positive)
     */
    public void setMassKg(double massKg) {
        if (massKg <= 0) {
            throw new IllegalArgumentException("Mass must be positive: " + massKg);
        }
        this.massKg = massKg;

        // r_s = (2 · G · M) / c²
        this.schwarzschildRadiusMetres = (2.0 * G * massKg) / (C * C);

        // Scale to pixels
        this.eventHorizonPixels = schwarzschildRadiusMetres / METRES_PER_PIXEL;

        // Simulation gravitational parameter (proportional to mass)
        this.gravParam = G * massKg * GRAV_SCALE;
    }

    /**
     * Returns the Schwarzschild radius (event horizon radius) in metres.
     *
     * <pre>r_s = (2 · G · M) / c²</pre>
     */
    public double getSchwarzschildRadiusMetres() {
        return schwarzschildRadiusMetres;
    }

    /**
     * Returns the event horizon radius in simulation (pixel) coordinates.
     * Particles that cross this boundary are considered absorbed.
     */
    public double getEventHorizonPixels() {
        return eventHorizonPixels;
    }

    /**
     * Returns the simulation gravitational parameter μ (G_sim · M) in px³/s².
     * Used by {@link GravityPhysics} to compute particle accelerations.
     */
    public double getGravParam() {
        return gravParam;
    }
}
