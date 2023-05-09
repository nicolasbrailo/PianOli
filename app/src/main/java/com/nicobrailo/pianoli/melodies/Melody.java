package com.nicobrailo.pianoli.melodies;

import java.util.Locale;

public class Melody {

    public static final Melody waltzing_matilda = fromString(
            "waltzing_matilda",
            "A A A A G G " +
                    // Once a jolly swagman`

                    "F A F D E F " +
                    // camped by a billabong

                    "C F A C2 C2 C2 " +
                    // under the shade of a

                    "C2 C2 C2 C2 " +
                    // coolibah tree

                    "F G A A A G G " +
                    // And he sang as he watched and

                    "F G A F D E F " +
                    // waited till his Billy boiled

                    "C F A C2 Bb A  " +
                    // You'll come a waltzing ma-

                    "G G G F " +
                    // -tilda with me.

                    "C2 C2 C2 C2 A " +
                    // Waltzing Matilda,

                    "F2 F2 E2 D2 C2 " +
                    // Waltzing Matilda,

                    "C2 C2 C2 D2 C2 C2 " +
                    // You'll come a waltzing Mat-

                    "C2 Bb A G F G " +
                    // -tilda with me, And he

                    "A A A G G " +
                    // Sang as he watched and

                    "F G A F D E F " +
                    // waited till his Billy boiled,

                    "C F A C2 Bb A " +
                    // You'll come a waltzing Ma-

                    "G G G F"
            // -tilda with me.
    );

    public static final Melody im_a_little_teapot = fromString(
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

    public static final Melody twinkle_twinkle_little_star = fromString(
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

    public static final Melody insy_winsy_spider = fromString(
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

    public static final Melody[] all = new Melody[]{
            twinkle_twinkle_little_star,
            insy_winsy_spider,
            im_a_little_teapot,
            waltzing_matilda
    };

    /**
     * A somewhat-robust string to melody parser.
     * Allows melodies to be specified as a string of notes, where the notes are: "A", "B1", "C#1", "Bb1", "G2", etc.
     * <p>
     * Notes are separated by whitespace.
     * <p>
     * Notes in the first octave can leave off the octave designation and it will be automatically
     * appended (i.e. "C" will become "C1"). This makes it simpler to write songs that fall within a single octave.
     */
    static Melody fromString(String id, String plainTextNotes) {
        String[] notes = plainTextNotes.trim().split("\\s+");

        for (int i = 0; i < notes.length; i++) {
            if (!notes[i].matches(".*\\d$")) {
                notes[i] = notes[i] + "1";
            }
        }
        return new Melody(id, notes);
    }

    private final String id;
    private final String[] notes;

    public Melody(String id, String[] notes) {
        this.id = id;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public String[] getNotes() {
        return notes;
    }
}
