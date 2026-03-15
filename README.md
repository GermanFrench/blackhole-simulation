# Black Hole Simulator

A scientific black hole physics simulator written in **Java 21** using **JavaFX**.
The application simulates gravitational interactions between a central black hole
and orbiting test particles, visualising trajectories, the event horizon, and
accretion-disk dynamics in real time.

![Simulation screenshot placeholder](docs/screenshot.png)

---

## Table of Contents

1. [Physics Background](#physics-background)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [Building and Running](#building-and-running)
5. [Controls](#controls)
6. [Scenarios](#scenarios)
7. [Project Structure](#project-structure)

---

## Physics Background

### Schwarzschild Radius

The **Schwarzschild radius** is the event-horizon radius of a non-rotating,
uncharged black hole.  It is derived from the condition that the escape
velocity equals the speed of light *c*:

```
r_s = (2 · G · M) / c²
```

| Symbol | Meaning | Value |
|--------|---------|-------|
| G | Gravitational constant | 6.67430 × 10⁻¹¹ N·m²/kg² |
| M | Black hole mass | kg (adjustable via slider) |
| c | Speed of light | 2.99792458 × 10⁸ m/s |

Any object that crosses inside r_s cannot escape, regardless of its speed —
even light.  In the simulation, particles that cross the event horizon are
immediately absorbed and removed.

### Gravitational Acceleration

Each test particle experiences Newtonian gravitational acceleration towards
the black hole:

```
a = μ / r²     where μ = G · M  (standard gravitational parameter)
```

The direction of acceleration always points from the particle toward the
black hole centre.  A small **softening length ε = 1 px** is added to the
denominator to prevent numerical divergence near the singularity:

```
a = μ / (r² + ε²)
```

### Circular Orbital Speed

For a stable circular orbit at radius *r*:

```
v_circ = √(μ / r)
```

The accretion-disk scenario uses this formula to initialise particles in
quasi-circular orbits.

### Numerical Integration — RK4

Particle trajectories are integrated using the **4th-order Runge-Kutta
(RK4)** method, which is far more accurate than simple Euler integration for
orbital mechanics.  Given state y = (position, velocity):

```
k1 = f(yₙ)
k2 = f(yₙ + h/2 · k1)
k3 = f(yₙ + h/2 · k2)
k4 = f(yₙ + h   · k3)

y_{n+1} = yₙ + (h/6) · (k1 + 2·k2 + 2·k3 + k4)
```

The simulation runs at a fixed time step of **1/60 s** per frame, matching
the JavaFX AnimationTimer target of 60 fps.

### Photon Sphere

The **photon sphere** at 1.5 × r_s is where circular photon orbits are
possible.  It is shown in the renderer as a thin golden ring.

---

## Architecture

```
blackhole-simulator
 ├── physics
 │   ├── Vector2D.java        Immutable 2-D vector (add, scale, normalise, …)
 │   ├── BlackHole.java       Schwarzschild radius, event horizon, grav. param
 │   ├── Particle.java        Position, velocity, trail buffer, absorption flag
 │   └── GravityPhysics.java  Acceleration formula, event-horizon detection
 │
 ├── simulation
 │   ├── SimulationEngine.java  AnimationTimer loop, particle lifecycle
 │   └── Integrator.java        RK4 and symplectic-Euler integrators
 │
 ├── rendering
 │   ├── SpaceRenderer.java     Background, star field, accretion glow, event horizon
 │   └── ParticleRenderer.java  Particle dots and fading trajectory trails
 │
 ├── ui
 │   ├── MainApp.java           JavaFX Application entry point
 │   └── ControlPanel.java      Start/Pause, Reset, sliders, statistics
 │
 └── scenarios
     ├── AccretionDiskScenario.java  Particles in quasi-circular orbits
     └── EventHorizonScenario.java   Particles plunging towards the black hole
```

### Key Design Decisions

* **Immutable `Vector2D`** — all arithmetic returns new objects, preventing
  accidental mutation of shared physics state.
* **Simulation coordinates in pixels** — keeps rendering simple while a
  documented `METRES_PER_PIXEL` constant bridges to SI units for the
  Schwarzschild formula.
* **RK4 as default integrator** — preserves orbital energy significantly
  better than Euler over many frames; Euler remains available as a reference.
* **Fixed physics timestep** — the AnimationTimer elapsed time is *clamped*
  to `PHYSICS_DT_SECONDS` so that GC pauses or heavy frames do not cause a
  sudden large step that destabilises orbits.

---

## Prerequisites

| Tool | Minimum version |
|------|----------------|
| Java | 21 |
| Maven | 3.8+ |

JavaFX is downloaded automatically by Maven from Maven Central.

---

## Building and Running

### With Maven (recommended)

```bash
# Compile and run tests
JAVA_HOME=/path/to/jdk-21 mvn test

# Launch the simulator
JAVA_HOME=/path/to/jdk-21 mvn javafx:run
```

### Build a fat-jar

```bash
JAVA_HOME=/path/to/jdk-21 mvn package
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.graphics \
     -jar target/blackhole-simulator-1.0.0-jar-with-dependencies.jar
```

> **Tip:** On macOS, add `-Djavafx.macosx.embedded=true` if the window
> does not appear.

### With Gradle

A Gradle wrapper is not included in this starter, but you can add the
[JavaFX Gradle plugin](https://github.com/openjfx/javafx-gradle-plugin) and
mirror the Maven dependency declarations.

---

## Controls

| Control | Description |
|---------|-------------|
| **▶ Start / ⏸ Pause** | Toggle the simulation loop |
| **↺ Reset** | Remove all particles and redraw the idle scene |
| **🌀 Accretion Disk** | Load the accretion-disk scenario (100 particles) |
| **⬤ Event Horizon** | Load the event-horizon plunge scenario (36 particles) |
| **Mass slider** | Scale the black hole mass from 0.1× to 5× the default; updates the Schwarzschild radius live |
| **Spawn count slider** | Set how many random particles to add |
| **✦ Spawn Random** | Add randomly-positioned particles with orbital velocities |

---

## Scenarios

### Accretion Disk (`AccretionDiskScenario`)

Spawns particles spread across five concentric rings between 2.5 × r_s and
7 × r_s.  Each particle is given the exact circular orbital speed for its
radius, plus a small random perturbation (±8%) that causes the orbits to
precess and gradually inspiral — mimicking a real accretion disk.

Colour transitions from hot orange/yellow near the inner edge to cooler
blue-white at the outer edge.

### Event Horizon Plunge (`EventHorizonScenario`)

Launches 36 particles from a radius of 8 × r_s, evenly distributed around
the black hole.  40% are given purely radial (straight-in) velocities; the
rest receive a random lateral nudge so some follow hyperbolic fly-by
trajectories and some are captured.

---

## License

MIT

