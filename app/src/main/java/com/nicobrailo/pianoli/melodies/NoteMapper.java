package com.nicobrailo.pianoli.melodies;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts most common musical notations (C1, D#1, Eb1, G♭1) to soundset-key indexes.
 */
public class NoteMapper {
    private static final Map<String, Integer> note_to_key_idx = new HashMap<>();

    static {
        note_to_key_idx.put("C1", 0);
        note_to_key_idx.put("C#1", 1);
        note_to_key_idx.put("Db1", 1);
        note_to_key_idx.put("D♭1", 1);
        note_to_key_idx.put("D1", 2);
        note_to_key_idx.put("D#1", 3);
        note_to_key_idx.put("Eb1", 3);
        note_to_key_idx.put("E♭1", 3);
        note_to_key_idx.put("E1", 4);

        note_to_key_idx.put("F1", 6);
        note_to_key_idx.put("F#1", 7);
        note_to_key_idx.put("Gb1", 7);
        note_to_key_idx.put("G♭1", 7);
        note_to_key_idx.put("G1", 8);
        note_to_key_idx.put("G#1", 9);
        note_to_key_idx.put("Ab1", 9);
        note_to_key_idx.put("A♭1", 9);
        note_to_key_idx.put("A1", 10);
        note_to_key_idx.put("A#1", 11);
        note_to_key_idx.put("Bb1", 11);
        note_to_key_idx.put("B♭1", 11);
        note_to_key_idx.put("B1", 12);

        note_to_key_idx.put("C2", 14);
        note_to_key_idx.put("C#2", 15);
        note_to_key_idx.put("Db2", 15);
        note_to_key_idx.put("D♭2", 15);
        note_to_key_idx.put("D2", 16);
        note_to_key_idx.put("D#2", 17);
        note_to_key_idx.put("Eb2", 17);
        note_to_key_idx.put("E♭2", 17);
        note_to_key_idx.put("E2", 18);

        note_to_key_idx.put("F2", 20);
        note_to_key_idx.put("F#2", 21);
        note_to_key_idx.put("Gb2", 21);
        note_to_key_idx.put("G♭2", 21);
        note_to_key_idx.put("G2", 22);
        note_to_key_idx.put("G#2", 23);
        note_to_key_idx.put("Ab2", 23);
        note_to_key_idx.put("A♭2", 23);
        note_to_key_idx.put("A2", 24);
        note_to_key_idx.put("A#2", 25);
        note_to_key_idx.put("Bb2", 25);
        note_to_key_idx.put("B♭2", 25);
        note_to_key_idx.put("B2", 26);
    }

    public static int get_key_idx_from_note(String note) {

        Integer key_idx = NoteMapper.note_to_key_idx.get(note);
        if (key_idx == null) {
            Log.w("PianOli::Piano", "Could not find a key corresponding to the note \"" + note + "\".");

            // 5 is designated as the special sound T.raw.no_note, so the app won't crash,
            // but it won't play a noise either.
            return 5;
        }

        return key_idx;
    }
}
