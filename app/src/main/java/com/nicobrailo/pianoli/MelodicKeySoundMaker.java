package com.nicobrailo.pianoli;


import com.nicobrailo.pianoli.melodies.MelodyPlayer;

import java.util.Objects;

/**
 * When key is pressed, plays the next note of the loaded melody, regardless of which key it was.
 *
 * @see MelodyPlayer
 *
 */
public class MelodicKeySoundMaker implements PianoListener {
    private final SoundSet soundSet;
    private final MelodyPlayer melody;

    public MelodicKeySoundMaker(SoundSet soundSet, MelodyPlayer melodyPlayer) {
        Objects.requireNonNull(soundSet, "Need a soundset to play notes from, otherwise I have no reason to exist");
        Objects.requireNonNull(melodyPlayer, "Need melody player to play, otherwise I have no reason to exist");

        this.soundSet = soundSet;
        this.melody = melodyPlayer;
    }

    @Override
    public void onKeyDown(int keyIdx) {
        if (!melody.hasNextNote()) {
            melody.reset();
        }

        soundSet.playNote(melody.nextNote());
    }

    @Override
    public void onKeyUp(int keyIdx) {
        // Nothing to do.
        // We don't stop already-running samples from playing, since they're short anyway.
    }
}
