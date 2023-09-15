package com.nicobrailo.pianoli.melodies;

import com.nicobrailo.pianoli.song.ImALittleTeapot;
import com.nicobrailo.pianoli.song.InsyWinsySpider;
import com.nicobrailo.pianoli.song.TwinkleTwinkleLittleStar;
import com.nicobrailo.pianoli.song.WaltzingMatilda;

/**
 * Parsed representation of a children's song.
 *
 * @see MelodyPlayer
 */
public class Melody {
    /**
     * All songs known to PianOli.
     *
     * <p>
     * We need a central catalog, primarily so we know which options to present in the settings screen.
     * It's not ideal to have this hardcoded, but the alternative (if we insist on song definitions in their own file)
     * is classpath scanning, which is a can of worms I'm not willing to go into.
     * </p>
     */
    public static final Melody[] all = new Melody[]{
            TwinkleTwinkleLittleStar.melody,
            InsyWinsySpider.melody,
            ImALittleTeapot.melody,
            WaltzingMatilda.melody
    };

    /**
     * A somewhat-robust string to melody parser.
     * Allows melodies to be specified as a string of notes, where the notes are: "A", "B1", "C#1", "Bb1", "G2", etc.
     * <p>
     * Notes are separated by whitespace.
     * <p>
     * Notes in the first octave can leave off the octave designation and it will be automatically
     * appended (i.e. "C" will become "C1"). This makes it simpler to write songs that fall within a single octave.
     *
     * @see NoteMapper
     */
    public static Melody fromString(String id, String plainTextNotes) {
        String[] notes = plainTextNotes.trim().split("\\s+");
        int[] parsedNotes = new int[notes.length];

        // Stream would be nicer, but we deliberately target ancient APIs,
        // so our game works on old phones (which people are more likely to give to small children)
        for (int i = 0; i < notes.length; i++) {
            parsedNotes[i] = NoteMapper.get_key_idx_from_note(notes[i]);
        }
        return new Melody(id, parsedNotes);
    }

    private final String id;
    private final int[] notes;

    public Melody(String id, int[] notes) {
        this.id = id;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public int[] getNotes() {
        return notes;
    }
}
