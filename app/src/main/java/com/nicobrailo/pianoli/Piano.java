package com.nicobrailo.pianoli;

import android.content.Context;
import android.media.SoundPool;
import android.util.Log;

class Piano {
    class Key {
        int x_i, x_f, y_i, y_f;

        Key(int x_i, int x_f, int y_i, int y_f) {
            this.x_i = x_i;
            this.x_f = x_f;
            this.y_i = y_i;
            this.y_f = y_f;
        }

        boolean contains(float pos_x, float pos_y) {
            return (pos_x > x_i && pos_x < x_f) &&
                    (pos_y > y_i && pos_y < y_f);
        }
    }

    private static final double KEYS_FLAT_HEIGHT_RATIO = 0.55;
    private static final int KEYS_WIDTH = 220;
    private static final int KEYS_FLAT_WIDTH = 130;

    private final int keys_height;
    private final int keys_flats_height;

    private final int keys_count;
    private boolean key_pressed[];

    Piano(final Context context, int screen_size_x, int screen_size_y) {
        keys_height = screen_size_y;
        keys_flats_height = (int) (screen_size_y * KEYS_FLAT_HEIGHT_RATIO);

        // Round up for possible half-key display
        final int big_keys = 1 + (screen_size_x / KEYS_WIDTH);
        // Count flats too
        keys_count = (big_keys * 2) + 1;

        key_pressed = new boolean[keys_count];
        for (int i = 0; i < key_pressed.length; ++i) key_pressed[i] = false;

        initSounds(context);
    }

    public int get_keys_count() {
        return keys_count;
    }

    public boolean is_key_pressed(int key_idx) {
        if (key_idx < 0 || key_idx >= key_pressed.length) {
            Log.d("PianOli::Piano", "This shouldn't happen: Sound out of range, key" + key_idx);
            return false;
        }

        return key_pressed[key_idx];
    }

    public void on_key_down(int key_idx) {
        if (key_idx < 0 || key_idx >= key_pressed.length) {
        Log.d("PianOli::Piano", "This shouldn't happen: Sound out of range, key" + key_idx);
        return;
    }
        key_pressed[key_idx] = true;
        play_sound(key_idx);
    }

    public void on_key_up(int key_idx) {
        if (key_idx < 0 || key_idx >= key_pressed.length) {
            Log.d("PianOli::Piano", "This shouldn't happen: Sound out of range, key" + key_idx);
            return;
        }

        key_pressed[key_idx] = false;
    }

    public int pos_to_key_idx(float pos_x, float pos_y) {
        final int big_key_idx = 2 * ((int) pos_x / KEYS_WIDTH);
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

    public Key get_area_for_key(int key_idx) {
        int x_i = key_idx / 2 * KEYS_WIDTH;
        return new Key(x_i, x_i + KEYS_WIDTH, 0, keys_height);
    }

    public Key get_area_for_flat_key(int key_idx) {
        final int octave_idx = (key_idx / 2) % 7;
        if (octave_idx == 2 || octave_idx == 6) {
            // Keys without flat get a null-area
            return new Key(0, 0, 0, 0);
        }

        final int offset = KEYS_WIDTH - (KEYS_FLAT_WIDTH / 2);
        int x_i = (key_idx / 2) * KEYS_WIDTH + offset;
        return new Key(x_i, x_i + KEYS_FLAT_WIDTH, 0, keys_flats_height);
    }



    private SoundPool KeySound;
    private int[]   KeySoundIdx;

    private void initSounds(final Context context) {
        if (KeySound != null) {
            // TODO: Useful to build new soundset
            KeySound.release();
        }

        KeySound = new SoundPool.Builder()
                                    .setMaxStreams(5)   // Play max 5 concurrent sounds
                                    .build();

        KeySoundIdx = new int[24];
        KeySoundIdx[ 0] = KeySound.load(context, R.raw.n01, 1);
        KeySoundIdx[ 1] = KeySound.load(context, R.raw.n02, 1);
        KeySoundIdx[ 2] = KeySound.load(context, R.raw.n03, 1);
        KeySoundIdx[ 3] = KeySound.load(context, R.raw.n04, 1);
        KeySoundIdx[ 4] = KeySound.load(context, R.raw.n05, 1);
        KeySoundIdx[ 5] = KeySound.load(context, R.raw.no_note, 1);
        KeySoundIdx[ 6] = KeySound.load(context, R.raw.n06, 1);
        KeySoundIdx[ 7] = KeySound.load(context, R.raw.n07, 1);
        KeySoundIdx[ 8] = KeySound.load(context, R.raw.n08, 1);
        KeySoundIdx[ 9] = KeySound.load(context, R.raw.n09, 1);
        KeySoundIdx[10] = KeySound.load(context, R.raw.n10, 1);
        KeySoundIdx[11] = KeySound.load(context, R.raw.n11, 1);
        KeySoundIdx[12] = KeySound.load(context, R.raw.n12, 1);
        KeySoundIdx[13] = KeySound.load(context, R.raw.no_note, 1);
        KeySoundIdx[14] = KeySound.load(context, R.raw.n13, 1);
        KeySoundIdx[15] = KeySound.load(context, R.raw.n14, 1);
        KeySoundIdx[16] = KeySound.load(context, R.raw.n15, 1);
        KeySoundIdx[17] = KeySound.load(context, R.raw.n15, 1);
        KeySoundIdx[18] = KeySound.load(context, R.raw.n16, 1);
        KeySoundIdx[19] = KeySound.load(context, R.raw.n17, 1);
        KeySoundIdx[20] = KeySound.load(context, R.raw.no_note, 1);
        KeySoundIdx[21] = KeySound.load(context, R.raw.n18, 1);
        KeySoundIdx[22] = KeySound.load(context, R.raw.n19, 1);
        KeySoundIdx[23] = KeySound.load(context, R.raw.n20, 1);
    }

    private void play_sound(final int key_idx) {
        if (key_idx < 0 || key_idx >= KeySoundIdx.length) {
            Log.d("PianOli::Piano", "This shouldn't happen: Sound out of range, key" + key_idx);
            return;
        }

        KeySound.play(KeySoundIdx[key_idx], 1, 1, 1, 0, 1f);
    }
}
