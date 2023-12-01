package com.nicobrailo.pianoli;

import com.nicobrailo.pianoli.melodies.Melody;
import com.nicobrailo.pianoli.melodies.SingleSongMelodyPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MelodicKeySoundMakerTest {

    /**
     * Melodic strategy shouldn't care about WHICH key is pressed,
     * so might as well commit all the way to absurdity.
     */
    public static final int ABSURD_NOTE = Integer.MAX_VALUE;

    private SpySoundSet spySoundSet;
    private SingleSongMelodyPlayer melodyPlayer;
    private MelodicKeySoundMaker soundMaker;

    @BeforeEach
    public void setup() {
        spySoundSet = new SpySoundSet();

        Melody melody = Melody.fromString("MelodicKeySoundMakerTest", "C D E F");
        melodyPlayer = new SingleSongMelodyPlayer(melody);

        soundMaker = new MelodicKeySoundMaker(spySoundSet, melodyPlayer);
    }

    @Test
    public void constructorEnsuresSoundSetNotNull() {
        assertThrows(NullPointerException.class, () -> new MelodicKeySoundMaker(null, melodyPlayer),
                "Melodic key-sound-maker should require a SoundSet to make sounds with.");

        assertThrows(NullPointerException.class, () -> new MelodicKeySoundMaker(spySoundSet, null),
                "Melodic key-sound-maker should require a Melody to extract notes from" );
    }

    @Test
    public void playsMelody() {
        // melodic strategy shouldn't care about WHICH key is pressed,
        // so might as well commit all the way to absurdity.
        int absurdNote = Integer.MAX_VALUE;

        // C
        soundMaker.onKeyDown(absurdNote);
        assertEquals(0, spySoundSet.lastPlayed);

        // D
        soundMaker.onKeyDown(absurdNote);
        assertEquals(2, spySoundSet.lastPlayed);

        // E
        soundMaker.onKeyDown(absurdNote);
        assertEquals(4, spySoundSet.lastPlayed);

        // F
        soundMaker.onKeyDown(absurdNote);
        assertEquals(6, spySoundSet.lastPlayed);
    }

    @Test
    public void loopsMelody() {
        // complete first loop (tested by #playsMelody() )
        soundMaker.onKeyDown(ABSURD_NOTE); // C
        soundMaker.onKeyDown(ABSURD_NOTE); // D
        soundMaker.onKeyDown(ABSURD_NOTE); // E
        soundMaker.onKeyDown(ABSURD_NOTE); // F

        // start of second loop
        soundMaker.onKeyDown(ABSURD_NOTE); // C, again
        assertEquals(0, spySoundSet.lastPlayed);
    }

    @Test
    void onKeyUpDoesNothing() {
        for (int i = 0; i <= SampledSoundSet.SOUNDSET_SAMPLES_SIZE; i++) {
            soundMaker.onKeyUp(i);
            assertEquals(SpySoundSet.NONE, spySoundSet.lastPlayed);
        }
    }
}
