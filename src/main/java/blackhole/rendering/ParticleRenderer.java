package blackhole.rendering;

import blackhole.physics.Particle;
import blackhole.physics.Vector2D;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Deque;
import java.util.List;

/**
 * Renders {@link Particle} objects and their trajectory trails onto a
 * JavaFX {@link GraphicsContext}.
 *
 * <h2>Trail Rendering</h2>
 * <p>Each particle stores up to {@link Particle#MAX_TRAIL_LENGTH} previous
 * positions.  The trail is drawn as a series of line segments, fading from
 * fully transparent at the oldest end to fully opaque at the current
 * position.</p>
 *
 * <h2>Colour Scheme</h2>
 * <p>Each particle is assigned a HSB hue at construction time.  The trail
 * uses the same hue at full saturation and brightness; only the alpha
 * decreases along the trail.</p>
 */
public final class ParticleRenderer {

    // Utility class.
    private ParticleRenderer() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Renders all particles (trail + dot) onto the given graphics context.
     *
     * @param gc        target graphics context (canvas)
     * @param particles list of active particles
     */
    public static void render(GraphicsContext gc, List<Particle> particles) {
        for (Particle p : particles) {
            drawTrail(gc, p);
            drawParticle(gc, p);
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Draws the position trail of a single particle as a fading polyline.
     *
     * <p>The oldest point in the trail has alpha → 0 and the newest
     * (just before the current position) has alpha → 0.6, giving a smooth
     * fade-out effect.</p>
     */
    private static void drawTrail(GraphicsContext gc, Particle particle) {
        Deque<Vector2D> trail = particle.getTrail();
        if (trail.isEmpty()) return;

        double hue  = particle.getHue();
        int    size = trail.size();
        int    idx  = 0;

        Vector2D[] pts = trail.toArray(new Vector2D[0]);

        for (int i = 0; i < pts.length - 1; i++) {
            // Normalised position along trail: 0 = oldest, 1 = newest
            double t     = (double) (i + 1) / size;
            double alpha = t * 0.65;
            double width = 1.0 + t * 1.5;

            Color trailColor = Color.hsb(hue, 1.0, 1.0, alpha);
            gc.setStroke(trailColor);
            gc.setLineWidth(width);
            gc.strokeLine(pts[i].x, pts[i].y, pts[i + 1].x, pts[i + 1].y);
        }

        // Final segment from last trail point to current position
        if (pts.length > 0) {
            double alpha = 0.65;
            Color trailColor = Color.hsb(hue, 1.0, 1.0, alpha);
            gc.setStroke(trailColor);
            gc.setLineWidth(2.5);
            Vector2D last = pts[pts.length - 1];
            Vector2D cur  = particle.getPosition();
            gc.strokeLine(last.x, last.y, cur.x, cur.y);
        }
    }

    /**
     * Draws the particle dot at its current position as a filled circle with
     * a small bright core and a softer outer glow.
     */
    private static void drawParticle(GraphicsContext gc, Particle particle) {
        double hue = particle.getHue();
        double cx  = particle.getPosition().x;
        double cy  = particle.getPosition().y;
        double r   = Particle.DISPLAY_RADIUS;

        // Outer soft glow
        gc.setFill(Color.hsb(hue, 0.8, 1.0, 0.35));
        gc.fillOval(cx - r * 2, cy - r * 2, r * 4, r * 4);

        // Bright core
        gc.setFill(Color.hsb(hue, 0.6, 1.0, 1.0));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        // White specular highlight
        gc.setFill(Color.rgb(255, 255, 255, 0.8));
        double highlightR = r * 0.4;
        gc.fillOval(cx - highlightR - r * 0.25,
                    cy - highlightR - r * 0.25,
                    highlightR * 2, highlightR * 2);
    }
}
