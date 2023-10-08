package com.nicobrailo.pianoli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class PianoCoordinateMapperTest {
    public static final int SIZE_X = 1920;
    public static final int SIZE_Y = 1080;

    /** 0.55 intentionally hardcoded instead of {@link Key#FLAT_HEIGHT_RATIO}, for test independence */
    public static final float FLAT_HEIGHT = SIZE_Y * 0.55f;

    /**
     * How often to repeat the randomness in test-loops.
     *
     * <p>
     * Choosing this too small will mean statistically failing the tests sometimes, because not enough random attempts
     * were done to guarantee all expected areas were hit.
     * </p>
     */
    public static final int ROUNDS = 3000;

    private Piano piano;
    private Random random;

    @BeforeEach
    void initPiano() {
        piano = new Piano(SIZE_X, SIZE_Y);
        random = new Random();
    }

    @Test
    void screenCorners() {
        assertEquals(0, piano.pos_to_key_idx(0,0));
        assertEquals(0, piano.pos_to_key_idx(0, SIZE_Y));
        assertEquals(16, piano.pos_to_key_idx(SIZE_X,0));
        assertEquals(16, piano.pos_to_key_idx(SIZE_X, SIZE_Y));
    }

    /**
     * Property-based test: 'Big' key indexes are always even; the flat keys are on the odd positions.
     *
     * <p>
     * Assert that we only get big-key indexes in the 'open' area below the flats.
     * </p>
     */
    @Test
    void mashingBottomAreaAlwaysEven() {

        SortedSet<Integer> seenKeys = new TreeSet<>();
        for (int i = 0; i < ROUNDS; i++) {
            float x = random.nextFloat() * SIZE_X; // anywhere along X axis
            float y = FLAT_HEIGHT + random.nextFloat() * (SIZE_Y - FLAT_HEIGHT); // at high-y "below" the flats

            int key_idx = piano.pos_to_key_idx(x,y);

            seenKeys.add(key_idx);
            assertTrue(isEven(key_idx),
                    String.format("big-key area somehow yielded a flat-key index %d for X,Y = %f, %f) ", key_idx, x, y));
        }

        // all even ints in range 0..17.
        Collection<Integer> expected = new TreeSet<>(List.of(0,2,4,6,8,10,12,14,16));
        assertEquals(expected, seenKeys, "Repeated mashing didn't hit all possible keys, this is statistically unlikely");
    }

    /**
     * Property-based test: statistically speaking, hitting the 'upper' area of the keyboard often enough
     * <em>should, eventually</em> hit all keys.
     */
    @Test
    void mashingFlatAreaHitAllKeys() {
        SortedSet<Integer> seenKeys = new TreeSet<>();
        for (int i = 0; i < ROUNDS; i++) {
            float x = random.nextFloat() * SIZE_X; // anywhere along X axis
            float y = random.nextFloat() * FLAT_HEIGHT; // at low-y, exclusively in the flats-containing region

            seenKeys.add(piano.pos_to_key_idx(x,y));
        }

        // all ints in range 0..17, except:
        //   5 = our first no-flat; (and thus also our canonical 'not-a-key' value
        //  13 = second not-a-flat position in an octave
        // The test-screen doesn't extend far enough to reach the first missing flat in the second octave (19).
        Collection<Integer> expected = new TreeSet<>(List.of(0,1,2,3,4,/*5,*/6,7,8,9,10,11,12,/*13,*/14,15,16,17));

        assertEquals(expected, seenKeys, "Repeated mashing didn't hit all possible keys, this is statistically unlikely");
    }

    private static boolean isEven(int key_idx) {
        return key_idx % 2 == 0;
    }
}
