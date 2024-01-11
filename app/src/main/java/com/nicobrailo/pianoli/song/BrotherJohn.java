package com.nicobrailo.pianoli.song;

import com.nicobrailo.pianoli.melodies.Melody;

/**
 * The massively translated Frère Jacques ("Are you sleeping, Brother John?")
 *
 * <p>
 * This nursery rhyme has been translated to dozens of languages, so it should reach a very wide audience.
 * </p>
 *
 * <p>
 * Compared to the classic C-D-E-C arrangement, this version is up-shifted by three full notes,
 * to account for the bass-note the final chord. Without this shift, it would fall below where we have sound samples.
 * </p>
 *
 *
 * <p>
 * Further reading:<ul>
 * <li><a href="https://en.wikipedia.org/wiki/Fr%C3%A8re_Jacques">English Wikipedia: Frère Jacques</a></li>
 * <li><a href="https://de.wikipedia.org/wiki/Fr%C3%A8re_Jacques">German Wikipedia: Frère Jacques</a>  (listing some 50 translations)</li>
 * </ul></p>
 */
public class BrotherJohn {
    public static final Melody melody = Melody.fromString(
            "brother_john",
            " " + // 'useless' string so that code formatting indentation nicely lines up
                    // Are you sleeping, 2x
                    "F G A F " +
                    "F G A F " +

                    // Brother John? 2x
                    "A A# C2 " +
                    "A A# C2 " +

                    // Morning bells are ringing! 2x
                    "C2 D2 C2 A# A F " +
                    "C2 D2 C2 A# A F " +

                    // Please come on! 2x
                    "F C F " +
                    "F C F "
    );
}
