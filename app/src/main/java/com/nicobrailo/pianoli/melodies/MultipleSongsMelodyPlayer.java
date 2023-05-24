package com.nicobrailo.pianoli.melodies;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Cycles through a collection of {@link SingleSongMelodyPlayer}'s. Each time a melody is completed, the
 * next is started. When the final melody is finished, it will return to the first note of the
 * first melody again.
 */
public class MultipleSongsMelodyPlayer implements MelodyPlayer {

    private final List<MelodyPlayer> songs;

    public MultipleSongsMelodyPlayer(@NonNull List<Melody> songs) {
        this.songs = new ArrayList<>(songs.size());
        for (Melody melody : songs) {
            this.songs.add(new SingleSongMelodyPlayer(melody));
        }
    }

    private int song_idx = 0;

    @Override
    public void reset() {
        songs.get(song_idx).reset();
        song_idx = 0;
        songs.get(0).reset();
    }

    /**
     * Cycle through all available {@link SingleSongMelodyPlayer} songs, and when the last note of the
     * last melody is hit, go back to the first again.
     */
    @Override
    public String nextNote() {
        if (!songs.get(song_idx).hasNextNote()) {
            songs.get(song_idx).reset();
            song_idx = (song_idx + 1) % songs.size();
            songs.get(song_idx).reset();
        }

        return songs.get(song_idx).nextNote();

    }

    @Override
    public boolean hasNextNote() {
        // If we are not on the last song, then there is definitely more notes to be played before
        // we should be reset(). If we are on the last song, then just ask that song if it has any
        // notes left.
        return song_idx < songs.size() - 1 || songs.get(song_idx).hasNextNote();
    }
}
