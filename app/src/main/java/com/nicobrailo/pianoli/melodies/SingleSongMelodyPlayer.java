package com.nicobrailo.pianoli.melodies;

import java.util.Locale;
import java.util.NoSuchElementException;

public class SingleSongMelodyPlayer implements MelodyPlayer {

    private int melody_idx = 0;
    private Melody melody;

    SingleSongMelodyPlayer(Melody melody) {
        this.melody = melody;
    }

    @Override
    public String nextNote() {
        if (!hasNextNote()) {
            throw new NoSuchElementException();
        }
        String note = melody.getNotes()[melody_idx];
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
