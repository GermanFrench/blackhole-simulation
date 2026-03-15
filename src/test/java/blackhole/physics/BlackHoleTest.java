package blackhole.physics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BlackHole}.
 */
class BlackHoleTest {

    private static final double DELTA = 1e-6;

    @Test
    void testSchwarzschildRadiusFormula() {
        // r_s = (2 * G * M) / c²
        double mass     = BlackHole.DEFAULT_MASS_KG;
        double expected = (2.0 * BlackHole.G * mass) / (BlackHole.C * BlackHole.C);

        BlackHole bh = new BlackHole(Vector2D.ZERO, mass);
        assertEquals(expected, bh.getSchwarzschildRadiusMetres(), expected * DELTA);
    }

    @Test
    void testEventHorizonPixelsScaling() {
        BlackHole bh = new BlackHole(Vector2D.ZERO, BlackHole.DEFAULT_MASS_KG);
        double rsMetres = bh.getSchwarzschildRadiusMetres();
        double expectedPx = rsMetres / BlackHole.METRES_PER_PIXEL;
        assertEquals(expectedPx, bh.getEventHorizonPixels(), expectedPx * DELTA);
    }

    @Test
    void testGravParamProportionalToMass() {
        BlackHole bh1 = new BlackHole(Vector2D.ZERO, BlackHole.DEFAULT_MASS_KG);
        BlackHole bh2 = new BlackHole(Vector2D.ZERO, BlackHole.DEFAULT_MASS_KG * 2);
        // gravParam should double when mass doubles
        assertEquals(bh1.getGravParam() * 2, bh2.getGravParam(), bh1.getGravParam() * DELTA);
    }

    @Test
    void testEventHorizonRadiusProportionalToMass() {
        BlackHole bh1 = new BlackHole(Vector2D.ZERO, BlackHole.DEFAULT_MASS_KG);
        BlackHole bh2 = new BlackHole(Vector2D.ZERO, BlackHole.DEFAULT_MASS_KG * 3);
        // r_s ∝ M  →  doubling mass doubles event horizon radius
        assertEquals(bh1.getEventHorizonPixels() * 3,
                     bh2.getEventHorizonPixels(),
                     bh1.getEventHorizonPixels() * DELTA);
    }

    @Test
    void testSetMassKgUpdatesAllFields() {
        BlackHole bh = new BlackHole(Vector2D.ZERO);
        double oldRs  = bh.getSchwarzschildRadiusMetres();
        double oldGP  = bh.getGravParam();

        bh.setMassKg(BlackHole.DEFAULT_MASS_KG * 4);

        // All derived quantities should have updated
        assertEquals(oldRs * 4, bh.getSchwarzschildRadiusMetres(), oldRs * DELTA);
        assertEquals(oldGP * 4, bh.getGravParam(), oldGP * DELTA);
    }

    @Test
    void testNegativeMassThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> new BlackHole(Vector2D.ZERO, -1.0));
    }

    @Test
    void testZeroMassThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> new BlackHole(Vector2D.ZERO, 0.0));
    }

    @Test
    void testPosition() {
        Vector2D pos = new Vector2D(400, 300);
        BlackHole bh = new BlackHole(pos);
        assertEquals(pos, bh.getPosition());

        Vector2D newPos = new Vector2D(200, 150);
        bh.setPosition(newPos);
        assertEquals(newPos, bh.getPosition());
    }
}
