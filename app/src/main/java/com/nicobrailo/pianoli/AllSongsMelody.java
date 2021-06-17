package com.nicobrailo.pianoli;

/**
 * Cycles through a collection of {@link SingleSongMelody}'s. Each time a melody is completed, the
 * next is started. When the final melody is finished, it will return to the first note of the
 * first melody again.
 */
public class AllSongsMelody implements Melody {

    public static final Melody[] songs = new Melody[] {
            SingleSongMelody.twinkle_twinkle_little_star,
            SingleSongMelody.insy_winsy_spider,
            SingleSongMelody.im_a_little_teapot,
    };

    private int song_idx = 0;

    @Override
    public void reset() {
        songs[song_idx].reset();
        song_idx = 0;
        songs[0].reset();
    }

    /**
     * Cycle through all available {@link SingleSongMelody} songs, and when the last note of the
     * last melody is hit, go back to the first again.
     */
    @Override
    public String nextNote() {
        if (!songs[song_idx].hasNextNote()) {
            songs[song_idx].reset();
            song_idx = (song_idx + 1) % songs.length;
            songs[song_idx].reset();
        }

        return songs[song_idx].nextNote();

    }

    @Override
    public boolean hasNextNote() {
        // If we are not on the last song, then there is definitely more notes to be played before
        // we should be reset(). If we are on the last song, then just ask that song if it has any
        // notes left.
        return song_idx < songs.length - 1 || songs[song_idx].hasNextNote();
    }
}
