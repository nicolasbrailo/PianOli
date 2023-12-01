package com.nicobrailo.pianoli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StraightKeySoundMakerTest {
    private SpySoundSet spySoundSet;
    private StraightKeySoundMaker soundMaker;

    @BeforeEach
    public void setup() {
        spySoundSet = new SpySoundSet();
        soundMaker = new StraightKeySoundMaker(spySoundSet);

    }

    @Test
    public void constructorEnsuresSoundSetNotNull() {
        assertThrows(NullPointerException.class, () -> new StraightKeySoundMaker(null),
                "Straight key-sound-maker should require a SoundSet to make sounds with.");
    }

    @Test
    void onKeyDown() {
        // Simple for loop; Straight is such a simple implementation, @ParametrizedTest would be overkill
        for (int i = 0; i <= SampledSoundSet.SOUNDSET_SAMPLES_SIZE; i++) {
            soundMaker.onKeyDown(i);
            assertEquals(i, spySoundSet.lastPlayed,
                    "Straight key-sound-maker should pass on pressed key to soundset without interfering.");
        }
    }

    @Test
    void onKeyUpDoesNothing() {
        for (int i = 0; i <= SampledSoundSet.SOUNDSET_SAMPLES_SIZE; i++) {
            soundMaker.onKeyUp(i);
            assertEquals(SpySoundSet.NONE, spySoundSet.lastPlayed);
        }
    }
}
