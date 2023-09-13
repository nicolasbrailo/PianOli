package com.nicobrailo.pianoli.melodies;

public interface MelodyPlayer {
    int nextNote();
    boolean hasNextNote();
    void reset();
}
