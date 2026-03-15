package blackhole.simulation;

import blackhole.physics.BlackHole;
import blackhole.physics.GravityPhysics;
import blackhole.physics.Particle;
import blackhole.rendering.ParticleRenderer;
import blackhole.rendering.SpaceRenderer;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central simulation controller.
 *
 * <p>The engine owns:
 * <ul>
 *   <li>The {@link BlackHole} at the centre of the simulation.</li>
 *   <li>The mutable list of {@link Particle} objects.</li>
 *   <li>A JavaFX {@link AnimationTimer} that drives the update/render loop.</li>
 * </ul>
 * </p>
 *
 * <h2>Per-frame Loop</h2>
 * <ol>
 *   <li>Compute {@code dt} from the nanosecond timestamp provided by
 *       {@code AnimationTimer}.</li>
 *   <li>For each particle: integrate its position/velocity with
 *       {@link Integrator#stepRK4}.</li>
 *   <li>Check whether each particle has crossed the event horizon; if so,
 *       mark it as absorbed and remove it from the active list.</li>
 *   <li>Clear the canvas and render the scene via {@link SpaceRenderer} and
 *       {@link ParticleRenderer}.</li>
 * </ol>
 *
 * <h2>Time Step</h2>
 * <p>The simulation uses a fixed physical time step
 * ({@link #PHYSICS_DT_SECONDS}) per frame to keep the physics stable.  The
 * actual elapsed wall-clock time is measured but clamped so that a lag spike
 * does not cause an excessively large step that could destabilise orbits.</p>
 */
public class SimulationEngine {

    // -----------------------------------------------------------------------
    // Simulation parameters
    // -----------------------------------------------------------------------

    /**
     * Fixed physics time step in seconds.
     * The AnimationTimer targets ~60 fps, so this equals roughly one frame.
     * Clamped to this maximum to prevent large spikes from destabilising
     * the integrator.
     */
    public static final double PHYSICS_DT_SECONDS = 1.0 / 60.0;

    /**
     * Maximum number of particles that can be active at once.
     * Prevents unbounded memory growth when the user continuously spawns
     * particles.
     */
    public static final int MAX_PARTICLES = 500;

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private final BlackHole blackHole;
    private final List<Particle> particles = new ArrayList<>();
    private final Canvas canvas;

    private AnimationTimer animationTimer;
    private boolean running = false;
    private long lastNanoTime = -1;

    // Accumulated frame statistics (optional diagnostics)
    private int frameCount = 0;
    private int absorbedCount = 0;

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    /**
     * Creates a simulation engine bound to the given canvas.
     *
     * @param canvas    the JavaFX canvas to draw on
     * @param blackHole the black hole at the centre of the simulation
     */
    public SimulationEngine(Canvas canvas, BlackHole blackHole) {
        this.canvas    = canvas;
        this.blackHole = blackHole;
        buildAnimationTimer();
    }

    // -----------------------------------------------------------------------
    // AnimationTimer setup
    // -----------------------------------------------------------------------

    private void buildAnimationTimer() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long nowNano) {
                if (lastNanoTime < 0) {
                    lastNanoTime = nowNano;
                    return;
                }

                // Compute elapsed time, clamped to one fixed step to prevent
                // instability after pauses or garbage-collection pauses.
                double elapsed = (nowNano - lastNanoTime) * 1e-9;
                double dt = Math.min(elapsed, PHYSICS_DT_SECONDS);
                lastNanoTime = nowNano;

                update(dt);
                render();
            }
        };
    }

    // -----------------------------------------------------------------------
    // Control
    // -----------------------------------------------------------------------

    /**
     * Starts (or resumes) the simulation loop.
     * Has no effect if the simulation is already running.
     */
    public void start() {
        if (!running) {
            lastNanoTime = -1;
            running = true;
            animationTimer.start();
        }
    }

    /**
     * Pauses the simulation loop.
     * The canvas is not cleared; the last rendered frame remains visible.
     * Has no effect if the simulation is already paused.
     */
    public void pause() {
        if (running) {
            running = false;
            animationTimer.stop();
        }
    }

    /** Returns {@code true} if the simulation loop is currently running. */
    public boolean isRunning() {
        return running;
    }

    /**
     * Removes all particles and resets the frame counter.
     * The black hole is unchanged.  The canvas is cleared and the idle
     * background is redrawn.
     */
    public void reset() {
        particles.clear();
        frameCount    = 0;
        absorbedCount = 0;
        render(); // redraw empty scene
    }

    // -----------------------------------------------------------------------
    // Physics update
    // -----------------------------------------------------------------------

    /**
     * Advances the simulation by one time step {@code dt}.
     *
     * @param dt time step in seconds
     */
    private void update(double dt) {
        List<Particle> toRemove = new ArrayList<>();

        for (Particle p : particles) {
            // Integrate position and velocity using RK4
            Integrator.stepRK4(p, blackHole, dt);

            // Check event horizon crossing
            if (GravityPhysics.isInsideEventHorizon(p, blackHole)) {
                p.absorb();
                toRemove.add(p);
                absorbedCount++;
            }
        }

        particles.removeAll(toRemove);
        frameCount++;
    }

    // -----------------------------------------------------------------------
    // Rendering
    // -----------------------------------------------------------------------

    /** Renders the current simulation state onto the canvas. */
    private void render() {
        var gc = canvas.getGraphicsContext2D();
        SpaceRenderer.render(gc, canvas.getWidth(), canvas.getHeight(), blackHole);
        ParticleRenderer.render(gc, particles);
    }

    // -----------------------------------------------------------------------
    // Particle management
    // -----------------------------------------------------------------------

    /**
     * Adds a particle to the simulation.
     * If the particle limit is already reached, the oldest particle is
     * replaced so that fresh ones are always visible.
     *
     * @param particle particle to add
     */
    public void addParticle(Particle particle) {
        if (particles.size() >= MAX_PARTICLES) {
            particles.remove(0);
        }
        particles.add(particle);
    }

    /**
     * Adds multiple particles to the simulation.
     *
     * @param list particles to add
     */
    public void addParticles(List<Particle> list) {
        for (Particle p : list) {
            addParticle(p);
        }
    }

    /** Returns an unmodifiable view of the current particle list. */
    public List<Particle> getParticles() {
        return Collections.unmodifiableList(particles);
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    /** Returns the black hole controlled by this engine. */
    public BlackHole getBlackHole() {
        return blackHole;
    }

    /** Returns the total number of particles absorbed so far. */
    public int getAbsorbedCount() {
        return absorbedCount;
    }

    /** Returns the total number of frames rendered since the last reset. */
    public int getFrameCount() {
        return frameCount;
    }

    /** Returns the canvas this engine renders to. */
    public Canvas getCanvas() {
        return canvas;
    }
}
