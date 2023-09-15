package com.nicobrailo.pianoli.song;

import com.nicobrailo.pianoli.melodies.Melody;

public class InsyWinsySpider {
    public static final Melody melody = Melody.fromString(
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
}
