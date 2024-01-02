package com.nicobrailo.pianoli.melodies;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

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

    /**
     *
     * half-steps that don't have a black key in-between.
     *
     * <p>"Enharmonic equivalents" are different notations for the same note.
     * "B#", or half-step up from B, is equal to a normal "C".
     * The reasons for this are grounded in history and musical theory, but suffice to say:
     * many things in notation become easier if the half-step modifiers #/b are <em>always</em> allowed,
     * instead of forbidden on the notes missing their black-key.
     * </p>
     * <p>
     * Further reading:<ul>
     *     <li><a href="https://www.reddit.com/r/musictheory/comments/2rxyyg/can_someone_please_explain_why_b_equals_c/">Music Theory reddit: Can someone please explain why B# equals C?</a></li>
     * </ul>
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "B#,C",
            "Cb,B",
            "E#,F",
            "Fb,E",

    })
    public void enharmonicEquivalantsAreEqual(String modifiedNote, String plainEquivalent) {
        int modified = get_key_idx_from_note(modifiedNote);
        int plain = get_key_idx_from_note(plainEquivalent);
        assertEquals(plain, modified,
                "Enharmonic equivalent notation should work should work");

    }

    static List<Arguments> allNotesSource() {
        List<String> octaves = List.of("1", "2");
        List<String> baseNotes = List.of("C", "D", "E", "F", "G", "A", "B");
        List<String> modifiers = List.of("b", "", "#"); // ordered to keep resulting indexes in ascending order

        ArrayList<Arguments> result = new ArrayList<>(42);

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

                    result.add(Arguments.of(note));
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
