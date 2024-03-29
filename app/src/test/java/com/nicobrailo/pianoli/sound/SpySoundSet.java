package com.nicobrailo.pianoli.sound;

/**
 * observable dummy {@link SoundSet}, for test purposes.
 */
public class SpySoundSet implements SoundSet {
    public static final int NONE = Integer.MIN_VALUE;
    public int lastPlayed = NONE;

    @Override
    public void playNote(int keyIdx) {
        lastPlayed = keyIdx;
    }

    @Override
    public void close() {
        // dummy implementation, do nothing
    }
}
