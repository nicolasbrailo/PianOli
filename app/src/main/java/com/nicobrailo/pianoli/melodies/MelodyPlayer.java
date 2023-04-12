package com.nicobrailo.pianoli.melodies;

public interface MelodyPlayer {
    String nextNote();
    boolean hasNextNote();
    void reset();
}
