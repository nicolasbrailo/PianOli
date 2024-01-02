package com.nicobrailo.pianoli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class PianoListenerTest {

    private Piano piano;
    private SpyListener listener;

    @BeforeEach
    public void setup() {
        piano = new Piano(80,80);
        listener = new SpyListener();
    }

    @Test
    public void listenerLifecycle() {
        // test add
        assertTrue(piano.addListener(listener),
                "first add of new listener should modify listener collection");

        // added listeners should actually trigger
        piano.doKeyDown(0);
        assertEquals(1, listener.downCount,
                "listener should fire for key presses");

        piano.doKeyUp(0);
        assertEquals(1, listener.upCount,
                "listener should fire for key releases");

        // test removal
        assertTrue(piano.removeListener(listener),
                "first removal of added listener should actually remove");

        // after removal, should no longer trigger
        piano.doKeyDown(0);
        assertEquals(1, listener.downCount,
                "removed listener should not increase trigger count for key presses");

        piano.doKeyUp(0);
        assertEquals(1, listener.upCount,
                "removed listener should not increase trigger count for key releases");
    }

    @Test
    public void pianoShouldPreventDoubleTriggers() {
        // test add
        assertTrue(piano.addListener(listener),
                "first add of new listener should modify listener collection");
        assertFalse(piano.addListener(listener),
                "Repeated addition of same listener should not modify collection");

        // test internals actually prevented double addition
        piano.doKeyDown(0);
        assertEquals(1, listener.downCount,
                "double-added listener should only fire once for key presses");

        piano.doKeyUp(0);
        assertEquals(1, listener.upCount,
                "double-added listener should only fire once for key releases");
    }

    @Test
    public void doubleRemoval() {
        assumeTrue(piano.addListener(listener),
                "should be able to add, to have something to remove.");

        assertTrue(piano.removeListener(listener),
                "first removal should work");
        assertFalse(piano.removeListener(listener),
                "second removal should find nothing to remove");
    }

    @Test
    public void nullSafeAdd() {
        //noinspection DataFlowIssue // intentionally violating @NonNull to test handling.
        assertThrows(NullPointerException.class, () -> piano.addListener(null), "null-listener should not be accepted");

        assertDoesNotThrow(() -> piano.doKeyDown(0),
                "after adding a null-listener, listener notification should not explode");
        assertDoesNotThrow(() -> piano.doKeyUp(0),
                "after adding a null-listener, listener notification should not explode");
    }

    @Test
    public void nullSafeRemove() {
        assertFalse(piano.removeListener(null), "null-listener should be silently accepted, but not do anything");
    }
}
