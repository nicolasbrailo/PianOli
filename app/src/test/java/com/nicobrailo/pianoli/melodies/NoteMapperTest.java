package com.nicobrailo.pianoli.melodies;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static com.nicobrailo.pianoli.melodies.NoteMapper.NO_NOTE;
import static com.nicobrailo.pianoli.melodies.NoteMapper.get_key_idx_from_note;
import static org.junit.jupiter.api.Assertions.*;

class NoteMapperTest {

    @Test
    public void noteRangeLimits() {
        assertEquals(0, get_key_idx_from_note("C1"),
                "lowest parsable note should be parsed");
        assertEquals(26, get_key_idx_from_note("B2"),
                "lowest parsable note should be parsed");

    }


    static List<String> allNotesSource() {
        List<String> octaves = List.of("1", "2");
        List<String> baseNotes = List.of("C", "D", "E", "F", "G", "A", "B");
        List<String> modifiers = List.of("b", "", "#"); // ordered to keep resulting indexes in ascending order

        ArrayList<String> result = new ArrayList<>(42);

        // loop order differs from notation order, to keep notes in roughly ascending order
        for (String octave: octaves) {
            for (String base: baseNotes) {
                for (String modifier: modifiers) {
                    String note = base + modifier + octave;

                    // special handling for notes outside our range limits, which the combinator could create
                    switch (note) {
                        case "Cb1": continue; // before first possible key; Keyboard starts at C1.
                        case "B#2": continue; // after last possible key; We don't have sound samples beyond B2.
                    }

                    result.add(note);
                }
            }
        }
        return result;
    }

    @ParameterizedTest
    @MethodSource("allNotesSource")
    public void allNotesWork(String note) {
        int idx = get_key_idx_from_note(note);

        assertNotEquals(NO_NOTE, idx);
    }

    @ParameterizedTest
    @ValueSource(strings = {"C#1", "Db1", "D♭1"})
    public void fancySynonyms(String note) {
        assertEquals(1, get_key_idx_from_note(note),
                "all synonyms for the same note should work, including fancy symbols");
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "fooooooo"})
    public void noNoteFallback(String notANote) {
        assertEquals(5, get_key_idx_from_note(notANote),
                "non-existing notes should fall back to the not-a-note special value");
    }

    @Test
    public void nullSafe() {
        assertEquals(5, get_key_idx_from_note(null),
                "Notemapper should gracefully handle null");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "C",                //  0, first note of octave
            "C#", "Db", "D♭",   //  1
            "A",                // 10
            "A#", "Bb","B♭",    // 11
            "B"                 // 12, last note of octave
    })
    public void firstOctaveAutoCompletion(String shortNote) {
        String fullNote = shortNote + "1";
        assertEquals(get_key_idx_from_note(fullNote), get_key_idx_from_note(shortNote),
                "NoteMapper should autocomplete ommited first octave");
    }
}
