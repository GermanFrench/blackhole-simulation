package blackhole.ui;

import blackhole.physics.BlackHole;
import blackhole.physics.GravityPhysics;
import blackhole.physics.Particle;
import blackhole.physics.Vector2D;
import blackhole.scenarios.AccretionDiskScenario;
import blackhole.scenarios.EventHorizonScenario;
import blackhole.simulation.SimulationEngine;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * JavaFX control panel that sits alongside the simulation canvas.
 *
 * <p>Provides:
 * <ul>
 *   <li>Start / Pause toggle button.</li>
 *   <li>Reset button (clears all particles).</li>
 *   <li>Scenario buttons (Accretion Disk, Event Horizon).</li>
 *   <li>Black hole mass slider.</li>
 *   <li>Particle spawn count slider + spawn button.</li>
 *   <li>Live statistics labels (particle count, absorbed count).</li>
 * </ul>
 * </p>
 */
public class ControlPanel extends VBox {

    // -----------------------------------------------------------------------
    // Layout constants
    // -----------------------------------------------------------------------

    private static final double PANEL_WIDTH   = 230.0;
    private static final double SPACING       = 10.0;
    private static final Insets PADDING       = new Insets(16);

    // -----------------------------------------------------------------------
    // Mass slider range
    // -----------------------------------------------------------------------

    /** Minimum mass multiplier (0.1 × default). */
    private static final double MASS_SLIDER_MIN = 0.1;
    /** Maximum mass multiplier (5 × default). */
    private static final double MASS_SLIDER_MAX = 5.0;

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private final SimulationEngine engine;

    // Controls (kept as fields so they can be updated in event handlers)
    private final Button   startPauseButton;
    private final Label    particleCountLabel;
    private final Label    absorbedCountLabel;
    private final Slider   massSlider;
    private final Slider   spawnCountSlider;
    private final Label    massValueLabel;
    private final Label    rsValueLabel;

    /** Random number generator for random particle spawning. */
    private final Random rng = new Random();

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    /**
     * Creates the control panel and wires it to the given simulation engine.
     *
     * @param engine the running (or paused) simulation engine
     */
    public ControlPanel(SimulationEngine engine) {
        this.engine = engine;

        setPrefWidth(PANEL_WIDTH);
        setSpacing(SPACING);
        setPadding(PADDING);
        setStyle("-fx-background-color: #0a0a14; -fx-border-color: #2a2a4a; "
                 + "-fx-border-width: 1;");
        setAlignment(Pos.TOP_CENTER);

        // ---- Title ----
        Text title = new Text("Black Hole Simulator");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        title.setFill(Color.LIGHTYELLOW);

        // ---- Simulation section ----
        Label simLabel = sectionLabel("Simulation");

        startPauseButton = styledButton("▶  Start", "#1e6e3e");
        Button resetButton = styledButton("↺  Reset", "#5a1a1a");

        // ---- Scenario section ----
        Label scenarioLabel = sectionLabel("Scenarios");

        Button diskButton   = styledButton("🌀  Accretion Disk",  "#1a3a6e");
        Button plungeButton = styledButton("⬤  Event Horizon",   "#4a1a6e");

        // ---- Black hole mass section ----
        Label massLabel = sectionLabel("Black Hole Mass");
        massValueLabel  = infoLabel("Mass: 1.00×");
        rsValueLabel    = infoLabel(formatRs(BlackHole.DEFAULT_MASS_KG));

        massSlider = new Slider(MASS_SLIDER_MIN, MASS_SLIDER_MAX, 1.0);
        styleSlider(massSlider);

        // ---- Particle spawning section ----
        Label spawnLabel = sectionLabel("Spawn Particles");
        Label spawnCountValueLabel = infoLabel("Count: 20");

        spawnCountSlider = new Slider(1, 100, 20);
        styleSlider(spawnCountSlider);

        Button spawnButton = styledButton("✦  Spawn Random", "#2a4a1a");

        // ---- Statistics section ----
        Label statsLabel   = sectionLabel("Statistics");
        particleCountLabel = infoLabel("Active: 0");
        absorbedCountLabel = infoLabel("Absorbed: 0");

        // ---- Assemble layout ----
        getChildren().addAll(
                title,
                new Separator(),

                simLabel,
                startPauseButton,
                resetButton,
                new Separator(),

                scenarioLabel,
                diskButton,
                plungeButton,
                new Separator(),

                massLabel,
                massValueLabel,
                rsValueLabel,
                massSlider,
                new Separator(),

                spawnLabel,
                spawnCountValueLabel,
                spawnCountSlider,
                spawnButton,
                new Separator(),

                statsLabel,
                particleCountLabel,
                absorbedCountLabel
        );

        // ---- Wire event handlers ----

        startPauseButton.setOnAction(e -> toggleStartPause());
        resetButton.setOnAction(e -> handleReset());

        diskButton.setOnAction(e -> {
            handleReset();
            engine.addParticles(AccretionDiskScenario.createDefault(engine.getBlackHole()));
            if (!engine.isRunning()) toggleStartPause();
        });

        plungeButton.setOnAction(e -> {
            handleReset();
            engine.addParticles(EventHorizonScenario.createDefault(engine.getBlackHole()));
            if (!engine.isRunning()) toggleStartPause();
        });

        massSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double multiplier = newVal.doubleValue();
            double newMass    = BlackHole.DEFAULT_MASS_KG * multiplier;
            engine.getBlackHole().setMassKg(newMass);
            massValueLabel.setText(String.format("Mass: %.2f×", multiplier));
            rsValueLabel.setText(formatRs(newMass));
        });

        spawnCountSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                spawnCountValueLabel.setText("Count: " + newVal.intValue()));

        spawnButton.setOnAction(e -> spawnRandomParticles());

        // ---- Statistics refresh (every frame via engine listener would be ideal;
        //      using a simple periodic update driven by the animation timer instead) ----
        javafx.animation.AnimationTimer statsTimer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                particleCountLabel.setText("Active:   " + engine.getParticles().size());
                absorbedCountLabel.setText("Absorbed: " + engine.getAbsorbedCount());
            }
        };
        statsTimer.start();
    }

    // -----------------------------------------------------------------------
    // Event handlers
    // -----------------------------------------------------------------------

    private void toggleStartPause() {
        if (engine.isRunning()) {
            engine.pause();
            startPauseButton.setText("▶  Resume");
            startPauseButton.setStyle(buttonStyle("#1e6e3e"));
        } else {
            engine.start();
            startPauseButton.setText("⏸  Pause");
            startPauseButton.setStyle(buttonStyle("#6e5a1a"));
        }
    }

    private void handleReset() {
        boolean wasRunning = engine.isRunning();
        engine.pause();
        engine.reset();
        startPauseButton.setText("▶  Start");
        startPauseButton.setStyle(buttonStyle("#1e6e3e"));
        if (wasRunning) {
            engine.start();
            startPauseButton.setText("⏸  Pause");
            startPauseButton.setStyle(buttonStyle("#6e5a1a"));
        }
    }

    /**
     * Spawns a number of particles (from the slider value) at random
     * positions in the outer region of the canvas with orbital velocities.
     */
    private void spawnRandomParticles() {
        int count = (int) spawnCountSlider.getValue();
        BlackHole bh = engine.getBlackHole();
        double eh    = bh.getEventHorizonPixels();

        double canvasW = engine.getCanvas().getWidth();
        double canvasH = engine.getCanvas().getHeight();
        double cx      = bh.getPosition().x;
        double cy      = bh.getPosition().y;

        double minR = eh * AccretionDiskScenario.INNER_RADIUS_FACTOR;
        double maxR = Math.min(canvasW, canvasH) * 0.45;

        List<Particle> newParticles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle  = rng.nextDouble() * 2 * Math.PI;
            double radius = minR + rng.nextDouble() * (maxR - minR);

            Vector2D pos    = bh.getPosition()
                                .add(Vector2D.fromPolar(angle, radius));
            double   vCirc  = GravityPhysics.circularOrbitalSpeed(bh, radius);
            double   speed  = vCirc * (0.7 + rng.nextDouble() * 0.6);

            // Random mix of prograde / retrograde
            int dir = rng.nextBoolean() ? 1 : -1;
            Vector2D vel = new Vector2D(
                    -Math.sin(angle) * speed * dir,
                     Math.cos(angle) * speed * dir
            );

            double hue = rng.nextDouble() * 360;
            newParticles.add(new Particle(pos, vel, hue));
        }

        engine.addParticles(newParticles);
        if (!engine.isRunning()) {
            engine.start();
            startPauseButton.setText("⏸  Pause");
            startPauseButton.setStyle(buttonStyle("#6e5a1a"));
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 11));
        l.setTextFill(Color.LIGHTBLUE);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setPadding(new Insets(4, 0, 0, 0));
        return l;
    }

    private static Label infoLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", 11));
        l.setTextFill(Color.LIGHTGRAY);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private static Button styledButton(String text, String bgHex) {
        Button b = new Button(text);
        b.setStyle(buttonStyle(bgHex));
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private static String buttonStyle(String bgHex) {
        return "-fx-background-color: " + bgHex + "; "
             + "-fx-text-fill: #f0f0f0; "
             + "-fx-font-size: 12; "
             + "-fx-padding: 6 12; "
             + "-fx-cursor: hand; "
             + "-fx-background-radius: 4;";
    }

    private static void styleSlider(Slider slider) {
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(false);
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.setStyle("-fx-control-inner-background: #1a1a2e;");
    }

    /**
     * Formats the Schwarzschild radius value for display in the label.
     *
     * @param massKg black hole mass in kg
     * @return formatted string, e.g. "rₛ = 148,519 km"
     */
    private static String formatRs(double massKg) {
        double rsMetres = (2.0 * BlackHole.G * massKg) / (BlackHole.C * BlackHole.C);
        double rsKm     = rsMetres / 1000.0;
        if (rsKm >= 1e6) {
            return String.format("rₛ = %.3f × 10⁶ km", rsKm / 1e6);
        } else {
            return String.format("rₛ = %,.0f km", rsKm);
        }
    }
}
