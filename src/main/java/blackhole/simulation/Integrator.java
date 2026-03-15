package blackhole.simulation;

import blackhole.physics.BlackHole;
import blackhole.physics.GravityPhysics;
import blackhole.physics.Particle;
import blackhole.physics.Vector2D;

/**
 * Numerical integrators for advancing particle states through time.
 *
 * <h2>Integration Methods</h2>
 * <p>Two methods are provided:
 * <ul>
 *   <li><b>Euler</b> – first-order method, fast but accumulates error quickly
 *       at larger time steps.  Useful as a reference.</li>
 *   <li><b>RK4 (Runge-Kutta 4th order)</b> – fourth-order method that
 *       evaluates the derivatives at four intermediate points and combines
 *       them with a weighted average.  It conserves energy far better than
 *       Euler and is the default integrator.</li>
 * </ul>
 * </p>
 *
 * <h2>RK4 Summary</h2>
 * <p>Given state {@code y = (x, v)} and derivative function
 * {@code f(y) = (v, a(x))}, the RK4 update for step {@code h} is:</p>
 *
 * <pre>
 *   k1 = f(y_n)
 *   k2 = f(y_n + h/2 · k1)
 *   k3 = f(y_n + h/2 · k2)
 *   k4 = f(y_n + h   · k3)
 *
 *   y_{n+1} = y_n + (h/6) · (k1 + 2·k2 + 2·k3 + k4)
 * </pre>
 */
public final class Integrator {

    // Utility class.
    private Integrator() {}

    // -----------------------------------------------------------------------
    // RK4 (default)
    // -----------------------------------------------------------------------

    /**
     * Advances a single particle by one time step using the 4th-order
     * Runge-Kutta method.
     *
     * @param particle  the particle to update (mutated in place)
     * @param blackHole gravitational source
     * @param dt        time step in seconds
     */
    public static void stepRK4(Particle particle, BlackHole blackHole, double dt) {
        Vector2D pos0 = particle.getPosition();
        Vector2D vel0 = particle.getVelocity();
        Vector2D bhPos = blackHole.getPosition();
        double   mu    = blackHole.getGravParam();

        // --- Stage k1 ---
        Vector2D k1v = vel0;
        Vector2D k1a = GravityPhysics.computeAccelerationFromDelta(
                bhPos.subtract(pos0), mu);

        // --- Stage k2  (midpoint using k1 estimates) ---
        Vector2D pos2 = pos0.add(k1v.scale(dt * 0.5));
        Vector2D vel2 = vel0.add(k1a.scale(dt * 0.5));
        Vector2D k2v  = vel2;
        Vector2D k2a  = GravityPhysics.computeAccelerationFromDelta(
                bhPos.subtract(pos2), mu);

        // --- Stage k3  (midpoint using k2 estimates) ---
        Vector2D pos3 = pos0.add(k2v.scale(dt * 0.5));
        Vector2D vel3 = vel0.add(k2a.scale(dt * 0.5));
        Vector2D k3v  = vel3;
        Vector2D k3a  = GravityPhysics.computeAccelerationFromDelta(
                bhPos.subtract(pos3), mu);

        // --- Stage k4  (full step using k3 estimates) ---
        Vector2D pos4 = pos0.add(k3v.scale(dt));
        Vector2D vel4 = vel0.add(k3a.scale(dt));
        Vector2D k4v  = vel4;
        Vector2D k4a  = GravityPhysics.computeAccelerationFromDelta(
                bhPos.subtract(pos4), mu);

        // --- Combine: weighted average ---
        // new_pos = pos0 + (dt/6) * (k1v + 2*k2v + 2*k3v + k4v)
        Vector2D newPos = pos0.add(
                k1v.add(k2v.scale(2)).add(k3v.scale(2)).add(k4v)
                   .scale(dt / 6.0));

        // new_vel = vel0 + (dt/6) * (k1a + 2*k2a + 2*k3a + k4a)
        Vector2D newVel = vel0.add(
                k1a.add(k2a.scale(2)).add(k3a.scale(2)).add(k4a)
                   .scale(dt / 6.0));

        // Apply the final acceleration (used by renderer for diagnostics)
        Vector2D newAcc = GravityPhysics.computeAccelerationFromDelta(
                bhPos.subtract(newPos), mu);

        particle.setPosition(newPos);
        particle.setVelocity(newVel);
        particle.setAcceleration(newAcc);
    }

    // -----------------------------------------------------------------------
    // Euler (reference / fallback)
    // -----------------------------------------------------------------------

    /**
     * Advances a single particle by one time step using the symplectic
     * (semi-implicit) Euler method.
     *
     * <pre>
     *   v_{n+1} = v_n + a_n · dt
     *   x_{n+1} = x_n + v_{n+1} · dt   ← uses updated velocity
     * </pre>
     *
     * <p>Using the updated velocity for the position step (symplectic Euler)
     * makes this method area-preserving in phase space, which conserves
     * energy much better than the explicit (forward) Euler method for
     * oscillatory systems such as planetary orbits.</p>
     *
     * <p><b>Note:</b> Prefer {@link #stepRK4} for higher accuracy; this
     * method is provided as a simpler reference implementation.</p>
     *
     * @param particle  the particle to update (mutated in place)
     * @param blackHole gravitational source
     * @param dt        time step in seconds
     */
    public static void stepEuler(Particle particle, BlackHole blackHole, double dt) {
        Vector2D acc    = GravityPhysics.computeAcceleration(particle, blackHole);
        Vector2D newVel = particle.getVelocity().add(acc.scale(dt));
        // Symplectic step: use updated velocity for position
        Vector2D newPos = particle.getPosition().add(newVel.scale(dt));

        particle.setAcceleration(acc);
        particle.setVelocity(newVel);
        particle.setPosition(newPos);
    }
}
