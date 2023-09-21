package com.nicobrailo.pianoli.melodies;

import java.util.NoSuchElementException;

public class SingleSongMelodyPlayer implements MelodyPlayer {

    private int melody_idx = 0;
    private final Melody melody;

    SingleSongMelodyPlayer(Melody melody) {
        this.melody = melody;
    }

    @Override
    public int nextNote() {
        if (!hasNextNote()) {
            throw new NoSuchElementException();
        }
        int note = melody.getNotes()[melody_idx];
        melody_idx ++;
        return note;
    }

    @Override
    public void reset() {
        melody_idx = 0;
    }

    @Override
    public boolean hasNextNote() {
        return melody_idx < melody.getNotes().length;
    }

}
