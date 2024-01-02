package com.nicobrailo.pianoli;

/**
 * Any and all things that do <em>something</em> in response to key-presses on the {@link Piano}.
 */
public interface PianoListener {
    /**
     * Signals key <code>keyIdx</code> has been pressed.
     */
    void onKeyDown(int keyIdx);

    /**
     * signals key <code>keyIdx</code> has been released.
     */
    void onKeyUp(int keyIdx);
}
