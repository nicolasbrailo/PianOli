package com.nicobrailo.pianoli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Does our Piano function properly on tiny screens?
 *
 * <p>
 * Goal is to always show at least an octave.
 * </p>
 *
 * @see Piano#KEY_PREFERRED_WIDTH
 * @see Piano#MIN_NUMBER_OF_KEYS
 */
class PianoUndersizedScreensizeTest {
    private Piano tinyPiano;

    @BeforeEach
    void setup() {
        int tinyWidth = 74; // should end up with 10 pixels per big key, plus a bit for a partial first-key-of-next-octave
        tinyPiano = new Piano(tinyWidth, 10);
    }

    @Test
    void keyWidths() {
        assertEquals(10, tinyPiano.get_keys_width());
        assertEquals(6, tinyPiano.get_keys_flat_width());
    }

    @Test
    void alwaysShowAtLeastAnOctave() {
        // 17: 7 big keys for first octave, rounded up to +1 partial key at right edge of screen.
        //     each big key has a matching flat, for another +8
        //     flat keys are rounded up too (that sounds like it's not needed?) for another +1
        assertEquals(17, tinyPiano.get_keys_count());
    }
}
