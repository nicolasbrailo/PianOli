package com.nicobrailo.pianoli.melodies;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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
}
