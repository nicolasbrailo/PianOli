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
     * Signals key <code>keyIdx</code> has been released.
     */
    void onKeyUp(int keyIdx);

    /**
     * Signals that all keys should be released right now.
     * Called after the last onKeyUp.
     * This method is used purely as a safeguard - it should not add any new information.
     * But in case that there is a bug and the key down and up is unbalanced,
     * this can be used to reset to a valid state.
     */
    default void onAllKeysUp() {}
}
