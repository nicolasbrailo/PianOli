package com.nicobrailo.pianoli.melodies;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MelodyTest {

    /** JUnit5 arguments-adapter for {@link Melody} content */
    private static Stream<Arguments> enumerateAllMelodies() {
        return Arrays.stream(Melody.all)
                .map(m -> Arguments.of(m.getId(), m.getNotes()));
    }

    @ParameterizedTest(name = "[{index}] {0}") // name: avoid default note-array in testname, only songId
    @MethodSource("enumerateAllMelodies")
    public void allSongsParsable(String songId, int[] notes) {
            int noteIndex = 0;
            for (int note: notes) {
                assertNotEquals(NoteMapper.NO_NOTE, note,
                        String.format("Can't parse song %s, note at position %d not recognised",
                                songId, noteIndex));
                noteIndex++;
        }
    }
}
