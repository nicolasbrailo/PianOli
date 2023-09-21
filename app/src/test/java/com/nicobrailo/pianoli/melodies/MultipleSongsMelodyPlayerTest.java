package com.nicobrailo.pianoli.melodies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultipleSongsMelodyPlayerTest {
    MelodyPlayer player;

    @BeforeEach
    void initPlayer() {
        List<Melody> melodies = new ArrayList<>();
        melodies.add(new Melody("MultiSongPlayerTest_song1>", new int[] {11}));
        melodies.add(new Melody("MultiSongPlayerTest_song1>", new int[] {22}));
        melodies.add(new Melody("MultiSongPlayerTest_song1>", new int[] {33}));

        player = new MultipleSongsMelodyPlayer(melodies);
    }

    @Test
    void nextNote() {
        assertEquals(11, player.nextNote());
        assertEquals(22, player.nextNote());
        assertEquals(33, player.nextNote());

        assertEquals(11, player.nextNote(),
                "MultipleSongsMelodyPlayer should loop songs forever");
    }

    @Test
    void reset() {
        assertEquals(11, player.nextNote());
        assertEquals(22, player.nextNote());

        player.reset();

        assertEquals(11, player.nextNote());
        assertEquals(22, player.nextNote());
    }

    @Test
    void hasNextNote() {
        assertTrue(player.hasNextNote());
        assertEquals(11, player.nextNote());

        assertTrue(player.hasNextNote());
        assertEquals(22, player.nextNote());

        assertTrue(player.hasNextNote());
        assertEquals(33, player.nextNote());
        assertFalse(player.hasNextNote()); // after 33, before reset

        player.reset();
        assertTrue(player.hasNextNote());
        assertEquals(11, player.nextNote());
    }
}
