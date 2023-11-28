package com.nicobrailo.pianoli;

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
