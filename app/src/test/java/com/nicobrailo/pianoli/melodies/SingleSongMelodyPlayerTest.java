package com.nicobrailo.pianoli.melodies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class SingleSongMelodyPlayerTest {
    MelodyPlayer player;

    @BeforeEach
    void initPlayer() {
        player = new SingleSongMelodyPlayer(new Melody("SingleSongMelodyPlayerTest",
                new int[] {1,2,3}));
    }

    @Test
    void nextNote() {
        assertEquals(1, player.nextNote());
        assertEquals(2, player.nextNote());
        assertEquals(3, player.nextNote());

        assertThrows(NoSuchElementException.class, () -> player.nextNote());
    }

    @Test
    void reset() {
        assertEquals(1, player.nextNote());
        assertEquals(2, player.nextNote());
        assertEquals(3, player.nextNote());

        player.reset();

        assertEquals(1, player.nextNote());
    }

    @Test
    void hasNextNote() {
        assertTrue(player.hasNextNote());

        player.nextNote();
        assertTrue(player.hasNextNote());

        player.nextNote();
        assertTrue(player.hasNextNote());

        player.nextNote();
        assertFalse(player.hasNextNote());
    }
}
