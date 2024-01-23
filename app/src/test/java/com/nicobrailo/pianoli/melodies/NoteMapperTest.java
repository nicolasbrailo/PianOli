package com.nicobrailo.pianoli.melodies;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * Regression: Check enharmonics for half-steps that don't have a black key in-between.
     *
     * <p>
     * "Enharmonic equivalents" are different notations for the same note.
     * For example, a C# points at the same black key as Db. (half a step up from the previous key is the same
     * as half a step down from the next.)<br>
     * This gets slightly weird at the 'missing' black keys, where the "full" key-step is only half a step musically.
     * "B#", or half-step up from B, is equal to a normal "C".<br>
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
    @ParameterizedTest(name = "[{index}] {0} == {1}")
    @CsvSource({
            "C1,B#0", // special, below our first supported octave, but the modifier bumps it up.
            //"B0,Cb1", // unsupported; bringing first supported note down goes out of range.
            "F1,E#1",
            "E1,Fb1",
            "C2,B#1",
            "B1,Cb2",
            "F2,E#2",
            "E2,Fb2",
            "B2,Cb3", // special, beyond our first supported octave, but the modifier brings it down.
    })
    public void enharmonicsForMissingSmallKeysWork(String plainNote, String modifiedNote) {
        int plain = get_key_idx_from_note(plainNote);
        int modified = get_key_idx_from_note(modifiedNote);
        assertEquals(plain, modified,
                "Enharmonic equivalent notation should work even around the 'missing' small keys: "
        + plainNote + " should equal " + modifiedNote);

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

    /**
     * Derives fancy ♭/♯ versions of modified notes from {@link #allNotesSource()}.
     */
    static List<Arguments> allSynonymsSource() {
        return allNotesSource().stream()
                .filter(note -> note.contains("#") || note.contains("b")) // keep only notes with (non-fancy) modifiers
                .map(note -> Arguments.of(
                        note,
                        note
                                .replace('b', '♭')
                                .replace('#', '♯')
                ))
                .collect(Collectors.toList());
    }

    @ParameterizedTest(name = "[{index}] {0} == {1}")
    @MethodSource("allSynonymsSource")
    public void fancySynonyms(String drabNote, String fancyNote) {
        assertEquals(get_key_idx_from_note(drabNote), get_key_idx_from_note(fancyNote),
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
