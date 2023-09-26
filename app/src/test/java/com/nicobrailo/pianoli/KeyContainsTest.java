package com.nicobrailo.pianoli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class KeyContainsTest {

    private Key key;

    @BeforeEach
    void initKey() {
        key = new Key(0, 10, 0, 10);
    }

    @ParameterizedTest
    @CsvSource({
            "0.00001,0.000001",
            "0.00001,9.99999",
            "9.99999,0.00001",
            "9.99999,9.99999",
            "5,5"})
    void insideCounts(float x, float y) {
        assertTrue(key.contains(x, y), "inside points should be contained");
    }

    @ParameterizedTest
    @CsvSource({
            "-0.00001,-0.000001",
            "0,10.00001", "10.00001,0",
            "10.00001,10.00001",
            "1000,1000",
            "-1000,-1000"})
    void outside(float x, float y) {
        assertFalse(key.contains(x, y), "outside points shouldn't be contained");
    }

    /**
     * Like in Tennis or in Volleybal, "line is out"
     */
    @ParameterizedTest
    @CsvSource({"0,0", "0,10", "10,0", "10,10", "5,0", "0,5", "10,5", "5,10"})
    void edgesShouldntCount(float x, float y) {
        assertFalse(key.contains(x,y));
    }

    /**
     * Note that, since edges don't count on <em>all</em> sides, there is an (infinitely thin) area <em>between</em>
     * adjacent keys that belongs to neither key.
     *
     * @see #edgesShouldntCount(float, float)
     */
    @Test
    void noMansLand() {
        int border = 10;
        Key left = new Key(0, border, -5, 5);
        Key right = new Key(border, 2*border, -5, 5);

        assertFalse(left.contains(border, 0));
        assertFalse(right.contains(border, 0));

        Key top = new Key(-5, 5, border, 2*border);
        Key bottom = new Key(-5, 5, 0, border);

        assertFalse(top.contains(0, border));
        assertFalse(bottom.contains(0, border ));
    }

    /**
     * Zero-area key is used as null-object for the non-existing flat-keys.
     */
    @Test
    void pointKeyShouldBeUntouchable() {
        assertFalse(Key.CANT_TOUCH_THIS.contains( 0, 0));
        assertFalse(Key.CANT_TOUCH_THIS.contains(-0, 0)); // negative-zero: fun with floats!
        assertFalse(Key.CANT_TOUCH_THIS.contains( 0,-0));
        assertFalse(Key.CANT_TOUCH_THIS.contains(-0,-0));
    }
}
