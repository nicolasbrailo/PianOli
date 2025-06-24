package com.nicobrailo.pianoli.melodies;

/**
 * Converts most common musical notations (C1, D#1, Eb1, G♭1) to soundset-key indexes.
 */
public class NoteMapper {

    /**
     * 5 is designated as the special sound T.raw.no_note, so the app won't crash, but it won't play a noise either.
     */
    public static final int NO_NOTE = 5;

    public static int get_key_idx_from_note(String note) {
        if (note == null || note.isEmpty()) {
            return NO_NOTE;
        }

        char lastChar = note.charAt(note.length() - 1);
        if (!Character.isDigit(lastChar)) {
            note = note+"1";
        }

        // simplify fancy modifiers, to reduce switch-case boilerplate.
        note = note
                .replace('♭', 'b')
                .replace('♯', '#');

        // Convert note notation to key-index.
        // This is *almost* regular enough to be a simple int-mapping function, but modifiers around the missing
        // small keys jump by 2, instead of the normal 1; a direct int-calculation would need special-casing for those
        // few jumps, making it less readable than this alternative (in this authors' opinion)
        switch (note) {
            // Octave 1
            case "B#0": // special, below our first supported octave, but the modifier bumps it up.
            case "C1":
                return 0;
            case "C#1":
            case "Db1":
                return 1;
            case "D1":
                return 2;
            case "D#1":
            case "Eb1":
                return 3;
            case "E1":
            case "Fb1":
                return 4;
            case "E#1":
            case "F1":
                return 6;
            case "F#1":
            case "Gb1":
                return 7;
            case "G1":
                return 8;
            case "G#1":
            case "Ab1":
                return 9;
            case "A1":
                return 10;
            case "A#1":
            case "Bb1":
                return 11;
            case "B1":
            case "Cb2":
                return 12;

            // Octave 2
            case "B#1":
            case "C2":
                return 14;
            case "C#2":
            case "Db2":
                return 15;
            case "D2":
                return 16;
            case "D#2":
            case "Eb2":
                return 17;
            case "E2":
            case "Fb2":
                return 18;
            case "E#2":
            case "F2":
                return 20;
            case "F#2":
            case "Gb2":
                return 21;
            case "G2":
                return 22;
            case "G#2":
            case "Ab2":
                return 23;
            case "A2":
                return 24;
            case "A#2":
            case "Bb2":
                return 25;
            case "B2":
            case "Cb3": // special, beyond our first supported octave, but the modifier brings it down.
                return 26;

            default:
                return NO_NOTE;
        }
    }
}
