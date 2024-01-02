package com.nicobrailo.pianoli;

class SpyListener implements PianoListener {
    /** Most recent {@link #onKeyDown(int)} this listener saw */
    public int lastDownIdx = -1;

    /** How often {@link #onKeyDown(int)} was triggered. */
    public int downCount = 0;

    /** Most recent {@link #onKeyUp(int)} this listener saw */
    public int lastUpIdx = -1;

    /** How often {@link #onKeyUp(int)} was triggered. */
    public int upCount = 0;

    @Override
    public void onKeyDown(int keyIdx) {
        lastDownIdx = keyIdx;
        downCount++;
    }

    @Override
    public void onKeyUp(int keyIdx) {
        lastUpIdx = keyIdx;
        upCount++;
    }
}
