package blackhole.rendering;

import blackhole.physics.BlackHole;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Bloom;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.Random;

/**
 * Renders the static (non-particle) elements of the simulation:
 * <ul>
 *   <li>Star-field background.</li>
 *   <li>Accretion glow around the event horizon.</li>
 *   <li>Event horizon (dark disc).</li>
 *   <li>Gravitational-lens ring.</li>
 * </ul>
 *
 * <p>All drawing is done on the JavaFX {@link GraphicsContext} that belongs
 * to the simulation canvas.  Each call to {@link #render} redraws the
 * entire background, so it must be called before
 * {@link ParticleRenderer#render} each frame.</p>
 */
public final class SpaceRenderer {

    // -----------------------------------------------------------------------
    // Star-field constants
    // -----------------------------------------------------------------------

    private static final int   NUM_STARS   = 200;
    private static final long  STAR_SEED   = 0xDEADBEEFL;

    /** Pre-computed star positions as pairs [x, y] in normalised [0,1] space. */
    private static final double[] STAR_X = new double[NUM_STARS];
    private static final double[] STAR_Y = new double[NUM_STARS];
    private static final double[] STAR_R = new double[NUM_STARS]; // radius in px

    static {
        Random rng = new Random(STAR_SEED);
        for (int i = 0; i < NUM_STARS; i++) {
            STAR_X[i] = rng.nextDouble();
            STAR_Y[i] = rng.nextDouble();
            STAR_R[i] = 0.5 + rng.nextDouble() * 1.5;
        }
    }

    // Utility class.
    private SpaceRenderer() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Renders the full background scene onto the given graphics context.
     *
     * @param gc        target graphics context
     * @param width     canvas width in pixels
     * @param height    canvas height in pixels
     * @param blackHole the black hole (determines position and event-horizon size)
     */
    public static void render(GraphicsContext gc,
                              double width, double height,
                              BlackHole blackHole) {
        drawBackground(gc, width, height);
        drawStars(gc, width, height);
        drawAccretionGlow(gc, blackHole);
        drawEventHorizon(gc, blackHole);
        drawLensRing(gc, blackHole);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /** Fills the canvas with deep-space black. */
    private static void drawBackground(GraphicsContext gc,
                                       double width, double height) {
        gc.setFill(Color.rgb(2, 2, 8));
        gc.fillRect(0, 0, width, height);
    }

    /** Draws the pre-computed star field. */
    private static void drawStars(GraphicsContext gc,
                                  double width, double height) {
        for (int i = 0; i < NUM_STARS; i++) {
            double brightness = 0.4 + (STAR_R[i] - 0.5) / 1.5 * 0.6;
            gc.setFill(Color.gray(brightness));
            double x = STAR_X[i] * width;
            double y = STAR_Y[i] * height;
            double r = STAR_R[i];
            gc.fillOval(x - r, y - r, r * 2, r * 2);
        }
    }

    /**
     * Draws a radial orange/amber glow around the event horizon to simulate
     * the hot accretion disk.
     */
    private static void drawAccretionGlow(GraphicsContext gc, BlackHole blackHole) {
        double cx = blackHole.getPosition().x;
        double cy = blackHole.getPosition().y;
        double eh = blackHole.getEventHorizonPixels();
        double glowRadius = eh * 4.0;

        RadialGradient glow = new RadialGradient(
                0, 0,
                cx, cy,
                glowRadius,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(255, 140,   0, 0.6)),
                new Stop(0.3, Color.rgb(200,  60,   0, 0.3)),
                new Stop(0.7, Color.rgb(100,   0,  50, 0.1)),
                new Stop(1.0, Color.rgb(  0,   0,   0, 0.0))
        );

        gc.setFill(glow);
        gc.fillOval(cx - glowRadius, cy - glowRadius,
                    glowRadius * 2, glowRadius * 2);
    }

    /** Draws the event-horizon disc (near-black centre). */
    private static void drawEventHorizon(GraphicsContext gc, BlackHole blackHole) {
        double cx = blackHole.getPosition().x;
        double cy = blackHole.getPosition().y;
        double eh = blackHole.getEventHorizonPixels();

        // Inner singularity gradient: dark purple core → pure black
        RadialGradient disc = new RadialGradient(
                0, 0,
                cx, cy,
                eh,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(10, 0, 20)),
                new Stop(0.7, Color.rgb( 5, 0, 10)),
                new Stop(1.0, Color.BLACK)
        );

        gc.setFill(disc);
        gc.fillOval(cx - eh, cy - eh, eh * 2, eh * 2);
    }

    /**
     * Draws a thin bright ring at the photon sphere (1.5 × r_s), simulating
     * gravitational lensing of background light.
     */
    private static void drawLensRing(GraphicsContext gc, BlackHole blackHole) {
        double cx = blackHole.getPosition().x;
        double cy = blackHole.getPosition().y;
        // Photon sphere is at 1.5 × Schwarzschild radius
        double ringRadius = blackHole.getEventHorizonPixels() * 1.5;
        double ringWidth  = Math.max(1.5, blackHole.getEventHorizonPixels() * 0.08);

        gc.setStroke(Color.rgb(255, 200, 80, 0.7));
        gc.setLineWidth(ringWidth);
        gc.strokeOval(cx - ringRadius, cy - ringRadius,
                      ringRadius * 2, ringRadius * 2);
    }
}
