package com.nicobrailo.pianoli.song;

import com.nicobrailo.pianoli.melodies.Melody;

public class ImALittleTeapot {
    public static final Melody melody = Melody.fromString(
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
}
