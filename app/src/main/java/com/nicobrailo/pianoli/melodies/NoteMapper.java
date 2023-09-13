package com.nicobrailo.pianoli.melodies;

import android.util.Log;


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

        switch (note) {
            // Octave 1
            case "C1":
                return 0;
            case "C#1":
            case "Db1":
            case "D♭1":
                return 1;
            case "D1":
                return 2;
            case "D#1":
            case "Eb1":
            case "E♭1":
                return 3;
            case "E1":
                return 4;
            case "F1":
                return 6;
            case "F#1":
            case "Gb1":
            case "G♭1":
                return 7;
            case "G1":
                return 8;
            case "G#1":
            case "Ab1":
            case "A♭1":
                return 9;
            case "A1":
                return 10;
            case "A#1":
            case "Bb1":
            case "B♭1":
                return 11;
            case "B1":
                return 12;

            // Octave 2
            case "C2":
                return 14;
            case "C#2":
            case "Db2":
            case "D♭2":
                return 15;
            case "D2":
                return 16;
            case "D#2":
            case "Eb2":
            case "E♭2":
                return 17;
            case "E2":
                return 18;
            case "F2":
                return 20;
            case "F#2":
            case "Gb2":
            case "G♭2":
                return 21;
            case "G2":
                return 22;
            case "G#2":
            case "Ab2":
            case "A♭2":
                return 23;
            case "A2":
                return 24;
            case "A#2":
            case "Bb2":
            case "B♭2":
                return 25;
            case "B2":
                return 26;

            default:
                Log.w("PianOli::Piano", "Could not find a key corresponding to the note \"" + note + "\".");
                return NO_NOTE;
        }
    }
}
