package blackhole.ui;

import blackhole.physics.BlackHole;
import blackhole.physics.Vector2D;
import blackhole.scenarios.AccretionDiskScenario;
import blackhole.simulation.SimulationEngine;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the Black Hole Simulator.
 *
 * <h2>Window Layout</h2>
 * <pre>
 *  ┌──────────────────────────────────────────┬──────────────┐
 *  │                                          │              │
 *  │          Simulation Canvas               │ControlPanel  │
 *  │           (BlackHole + Particles)        │              │
 *  │                                          │  [Start/Pause│
 *  │                                          │   Reset      │
 *  │                                          │   Scenarios  │
 *  │                                          │   Sliders]   │
 *  └──────────────────────────────────────────┴──────────────┘
 * </pre>
 *
 * <p>On startup the application loads the default
 * {@link AccretionDiskScenario} so the simulation is immediately engaging.</p>
 *
 * <h2>Run with Maven</h2>
 * <pre>
 *   JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 \
 *   mvn javafx:run
 * </pre>
 */
public class MainApp extends Application {

    // -----------------------------------------------------------------------
    // Window dimensions
    // -----------------------------------------------------------------------

    /** Width of the simulation canvas in pixels. */
    public static final double CANVAS_WIDTH  = 800.0;

    /** Height of the simulation canvas in pixels. */
    public static final double CANVAS_HEIGHT = 650.0;

    /** Width of the control panel in pixels. */
    public static final double PANEL_WIDTH   = 240.0;

    // -----------------------------------------------------------------------
    // JavaFX lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void start(Stage primaryStage) {
        // ---- Simulation canvas ----
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);

        // ---- Black hole: placed at canvas centre ----
        Vector2D centre   = new Vector2D(CANVAS_WIDTH / 2.0, CANVAS_HEIGHT / 2.0);
        BlackHole blackHole = new BlackHole(centre);

        // ---- Simulation engine ----
        SimulationEngine engine = new SimulationEngine(canvas, blackHole);

        // ---- Control panel ----
        ControlPanel controlPanel = new ControlPanel(engine);

        // ---- Root layout ----
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #02020a;");
        root.setCenter(canvas);
        root.setRight(controlPanel);

        // ---- Scene ----
        Scene scene = new Scene(root, CANVAS_WIDTH + PANEL_WIDTH, CANVAS_HEIGHT);
        scene.setFill(Color.rgb(2, 2, 10));

        // ---- Stage ----
        primaryStage.setTitle("Black Hole Simulator");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // ---- Load default scenario and start ----
        engine.addParticles(AccretionDiskScenario.createDefault(blackHole));
        engine.start();
    }

    // -----------------------------------------------------------------------
    // Main
    // -----------------------------------------------------------------------

    /**
     * Application entry point.  Launches the JavaFX runtime.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
