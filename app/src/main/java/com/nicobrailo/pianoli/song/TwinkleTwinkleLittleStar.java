package com.nicobrailo.pianoli.song;

import com.nicobrailo.pianoli.melodies.Melody;

public class TwinkleTwinkleLittleStar {
    public static final Melody melody = Melody.fromString(
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
}
