package com.nicobrailo.pianoli;


import java.util.Objects;

/**
 * Plays the note associated with a key, without any fancy mappings. I.E. "straight".
 */
public class StraightKeySoundMaker implements PianoListener {
    private final SoundSet soundSet;

    public StraightKeySoundMaker(SoundSet soundSet) {
        Objects.requireNonNull(soundSet, "Need a soundset to play notes from, otherwise I have no reason to exist");

        this.soundSet = soundSet;
    }

    @Override
    public void onKeyDown(int keyIdx) {
        soundSet.playNote(keyIdx);
    }

    @Override
    public void onKeyUp(int keyIdx) {
        // Nothing to do.
        // We don't stop already-running samples from playing, since they're short anyway.
    }
}
