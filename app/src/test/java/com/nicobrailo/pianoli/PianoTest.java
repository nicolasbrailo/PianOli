package com.nicobrailo.pianoli;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class PianoTest {
    private static Piano piano;

    @BeforeAll
    static void setup() {
        piano = new Piano(500, 80);
    }

    @ParameterizedTest
    @ValueSource(ints={-1, 18, Integer.MIN_VALUE, Integer.MAX_VALUE, -1000, +1000})
    void OutOfBoundsNeverPressed(int invalidIdx) {
        assertFalse(piano.is_key_pressed(invalidIdx));
    }


    static Stream<Arguments> allKeyIndexes() {
        return IntStream.range(0, piano.get_keys_count())
                .mapToObj(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("allKeyIndexes")
    void keyPressLifecycle(int i) {
        assertFalse(piano.is_key_pressed(i));

        piano.onKeyDown(i);
        assertTrue(piano.is_key_pressed(i));

        piano.onKeyUp(i);
        assertFalse(piano.is_key_pressed(i));
    }

    @ParameterizedTest
    @MethodSource("allKeyIndexes")
    void resetState(int i) {
        // prepare, set keyPressed to true
        // only assume it works, actual test-fails covered by #keyPressLifecycle
        // if an "assumption" fails, the test counts as "@Ignored".
        assumeFalse(piano.is_key_pressed(i));
        piano.onKeyDown(i);
        assumeTrue(piano.is_key_pressed(i));

        piano.resetState();

        assertFalse(piano.is_key_pressed(i));
    }
}
