package com.nicobrailo.pianoli;

import java.util.Locale;
import java.util.NoSuchElementException;

public class SingleSongMelody implements Melody {

    public static final SingleSongMelody im_a_little_teapot = SingleSongMelody.fromString(
            "im_a_little_teapot",
            "C D E F G C2 " +
            // I’m a little teapot

            "A C2 G " +
            // Short and stout

            "F F F E E " +
            // Here is my handle

            "D D D C " +
            // Here is my spout

            "C D E F G C2 " +
            // When I get all steamed up

            "A C2 G " +
            // Hear me shout

            "C2 A G G " +
            // “Tip me over

            "F E D C "
            // And pour me out!”
    );

    public static final SingleSongMelody twinkle_twinkle_little_star = SingleSongMelody.fromString(
            "twinkle_twinkle_little_star",
            "C C G G A A G " +
            // Twinkle, twinkle, little star

            "F F E E D D C " +
            // How I wonder what you are!

            "G G F F E E D " +
            // Up above the world so high,

            "G G F F E E D " +
            // Like a diamond in the sky...

            "C C G G A A G " +
            // Twinkle, twinkle, little star

            "F F E E D D C"
            // How I wonder what you are!
    );

    public static final SingleSongMelody insy_winsy_spider = SingleSongMelody.fromString(
            "insy_winsy_spider",
            "G1 C2 C2 C2 D2 E2 E2 " +
            // "Insy-winsy spider...

            "E2 D2 C2 D2 E2 C2 " +
            // "... climbed up the water spout.

            "E2 E2 F2 G2 " +
            // Down came the rain...

            "G2 F2 E2 F2 G2 E2 " +
            // ... and washed the spider out.

            "C2 C2 D2 E2 " +
            // Out came the sun...

            "E2 D2 C2 D2 E2 C2 " +
            // ... and dried up all the rain.

            "G1 G1 C2 C2 C2 D2 E2 E2 " +
            // Insy-winsy spider...

            "E2 D2 C2 D2 E2 C2"
            // ... climbed up the spout again.
    );

    public static final Melody[] all = new Melody[] {
            twinkle_twinkle_little_star,
            insy_winsy_spider,
            im_a_little_teapot,
    };

    /**
     * A somewhat-robust string to melody parser.
     * Allows melodies to be specified as a string of notes, where the notes are: "A", "B1", "C#1", "G2", etc.
     *
     * Notes are separated by whitespace.
     *
     * Notes in the first octave can leave off the octave designation and it will be automatically
     * appended (i.e. "C" will become "C1"). This makes it simpler to write songs that fall within a single octave.
     */
    static SingleSongMelody fromString(String id, String melody) {
        String[] notes = melody.trim().toUpperCase(Locale.ENGLISH).split("\\s+");

        for (int i = 0; i < notes.length; i ++) {
            if (!notes[i].matches(".*\\d$")) {
                notes[i] = notes[i] + "1";
            }
        }
        return new SingleSongMelody(id, notes);
    }

    private int melody_idx = 0;
    private final String id;
    private final String[] notes;

    SingleSongMelody(String id, String[] notes) {
        this.id = id;
        this.notes = notes;
    }

    @Override
    public String nextNote() {
        if (!hasNextNote()) {
            throw new NoSuchElementException();
        }
        String note = notes[melody_idx];
        melody_idx ++;
        return note;
    }

    @Override
    public void reset() {
        melody_idx = 0;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean hasNextNote() {
        return melody_idx < notes.length;
    }

}
