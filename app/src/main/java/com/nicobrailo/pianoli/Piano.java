package com.nicobrailo.pianoli;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Backing model / state of our virtual piano keyboard.
 *
 * <p>
 * Handles geometry, coordinate conversion and current state.
 * </p>
 * <p>
 * If you wish to do anything in response to key-presses/releases, implement a {@link PianoListener},
 * and register yourself via {@link #addListener(PianoListener)}.
 * You'll then be notified of each event.
 * </p>
 *
 * @see PianoCanvas
 * @see PianoListener
 */
class Piano {
    /**
     * Floor limit, if screensize dictates less than this amount of keys, start shrinking key width,
     * so we always display at least an octave.
     *
     * @see #KEY_PREFERRED_WIDTH
     */
    public static final int MIN_NUMBER_OF_KEYS = 7;
    /** Preferred width (device independent pixels) of a key, but see also {@link #MIN_NUMBER_OF_KEYS} */
    public static final int KEY_PREFERRED_WIDTH = 220;


    private final int keys_width;
    private final int keys_flat_width;
    private final int keys_height;
    private final int keys_flats_height;
    private final int keys_count;

    /** state tracker: which keys are <em>currently</em> pressed */
    private final boolean[] key_pressed;

    private final List<PianoListener> listeners;

    /**
     * Construct a partially initialised (geometry only) Piano model.
     *
     * @param screen_size_x the long dimension of the screen (keys are side-by-side along this axis)
     * @param screen_size_y the short dimension of the screen.
     */
    Piano(int screen_size_x, int screen_size_y) {
        keys_height = screen_size_y;
        keys_flats_height = (int) (screen_size_y * Key.FLAT_HEIGHT_RATIO);

        keys_width = Math.min(screen_size_x / MIN_NUMBER_OF_KEYS, KEY_PREFERRED_WIDTH);
        keys_flat_width = (int) (keys_width * Key.FLAT_WIDTH_RATIO);

        // Round up for possible half-key display
        final int big_keys = 1 + (screen_size_x / keys_width);
        // Count flats too
        // *2: Because ALL big keys get a matching flat-key, though for some its 0x0 pixels.
        // +1: not sure about this... The *2 already ensures a (partial) flat-key on the (partial) big-key.
        keys_count = (big_keys * 2) + 1;

        key_pressed = new boolean[keys_count]; // new array defaults to all false;
        listeners = new ArrayList<>();
    }

    int get_keys_flat_width() {
        return keys_flat_width;
    }

    int get_keys_width() {
        return keys_width;
    }

    int get_keys_count() {
        return keys_count;
    }

    void resetState() {
        Arrays.fill(key_pressed, false);
    }

    boolean is_key_pressed(int key_idx) {
        if (isOutOfRange(key_idx)) {
            Log.d("PianOli::Piano", "This shouldn't happen: isKeyPressed out of range, key" + key_idx);
            return false;
        }

        return key_pressed[key_idx];
    }

    /**
     * Switch key <code>keyIdx</code> to DOWN state, notifying all {@link PianoListener}s.
     *
     * @see PianoListener#onKeyDown(int)
     */
    public void doKeyDown(int keyIdx) {
        if (isOutOfRange(keyIdx)) {
            Log.d("PianOli::Piano", "This shouldn't happen: Key-Down out of range, key" + keyIdx);
            return;
        }

        Log.d("PianOli::Piano", "Key " + keyIdx + " is now DOWN");
        key_pressed[keyIdx] = true;

        for (PianoListener l : listeners) {
            l.onKeyDown(keyIdx);
        }
    }

    /**
     * Switch key <code>keyIdx</code> to UP state, notifying all {@link PianoListener}s.
     *
     * @see PianoListener#onKeyUp(int)
     */
    public void doKeyUp(int keyIdx) {
        if (isOutOfRange(keyIdx)) {
            Log.d("PianOli::Piano", "This shouldn't happen: Key-Up out of range, key" + keyIdx);
            return;
        }

        Log.d("PianOli::Piano", "Key " + keyIdx + " is now UP");
        key_pressed[keyIdx] = false;

        for (PianoListener l : listeners) {
            l.onKeyUp(keyIdx);
        }
    }

    private boolean isOutOfRange(int key_idx) {
        return key_idx < 0 || key_idx >= key_pressed.length;
    }

    /**
     * @param l new Listener to be notified
     * @return Per the {@link java.util.Collection#add(Object)} contract, <code>true</code> if the listener list changed as a result of this add,
     *          <code>false</code> if it was already subscribed.
     */
    public boolean addListener(PianoListener l) {
        if (l != null  // avoid NullPointerExceptions on notify
                && !listeners.contains(l)) { // don't double-add listeners, to avoid double-triggers
            listeners.add(l);
            return true;
        }
        return false;
    }

    /**
     * @param l Listener to unsubscribe.
     * @return Per the {@link java.util.Collection#remove(Object)} contract, <code>true</code> if the listener was removed,
     *          <code>false</code> if it wasn't found.
     */
    public boolean removeListener(PianoListener l) {
        return listeners.remove(l);
    }

    int pos_to_key_idx(float pos_x, float pos_y) {
        final int big_key_idx = 2 * ((int) pos_x / keys_width);
        if (pos_y > keys_flats_height) return big_key_idx;

        // Check if press is inside rect of flat key
        Key flat = get_area_for_flat_key(big_key_idx);
        if (flat.contains(pos_x, pos_y)) return big_key_idx + 1;

        if (big_key_idx > 0) {
            Key prev_flat = get_area_for_flat_key(big_key_idx - 2);
            if (prev_flat.contains(pos_x, pos_y)) return big_key_idx - 1;
        }

        // If not in the current or previous flat, it must be a hit in the big key
        return big_key_idx;
    }

    Key get_area_for_key(int key_idx) {
        int x_i = key_idx / 2 * keys_width;
        return new Key(x_i, x_i + keys_width, 0, keys_height);
    }

    Key get_area_for_flat_key(int key_idx) {
        final int octave_idx = (key_idx / 2) % 7;
        if (octave_idx == 2 || octave_idx == 6) {
            // Keys without flat get a null-area
            return Key.CANT_TOUCH_THIS;
        }

        final int offset = keys_width - (keys_flat_width / 2);
        int x_i = (key_idx / 2) * keys_width + offset;
        return new Key(x_i, x_i + keys_flat_width, 0, keys_flats_height);
    }
}
