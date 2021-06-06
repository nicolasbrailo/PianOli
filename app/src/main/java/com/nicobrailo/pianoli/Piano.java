package com.nicobrailo.pianoli;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Piano {
    private static final double KEYS_FLAT_HEIGHT_RATIO = 0.55;

    /**
     * Width of a flat key in relation to a regular key.
     */
    private static final double KEYS_FLAT_WIDTH_RATIO = 0.6;

    private static final int MIN_NUMBER_OF_KEYS = 7;
    private final int keys_width;
    private final int keys_flat_width;
    private final int keys_height;
    private final int keys_flats_height;
    private final int keys_count;
    private boolean[] key_pressed;
    private static SoundPool KeySound = null;
    private int[] KeySoundIdx;
    private List<Integer> melody = null;
    private int melody_idx;

    Piano(final Context context, int screen_size_x, int screen_size_y, final String soundset, final List<Integer> melody) {
        keys_height = screen_size_y;
        keys_flats_height = (int) (screen_size_y * KEYS_FLAT_HEIGHT_RATIO);

        keys_width = Math.min(screen_size_x / MIN_NUMBER_OF_KEYS, 220);
        keys_flat_width = (int) (keys_width * KEYS_FLAT_WIDTH_RATIO);

        // Round up for possible half-key display
        final int big_keys = 1 + (screen_size_x / keys_width);
        // Count flats too
        keys_count = (big_keys * 2) + 1;

        key_pressed = new boolean[keys_count];
        Arrays.fill(key_pressed, false);
        selectSoundset(context, soundset);

        this.melody = melody;
        this.melody_idx = 0;
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
        if (key_idx < 0 || key_idx >= key_pressed.length) {
            Log.d("PianOli::Piano", "This shouldn't happen: Sound out of range, key" + key_idx);
            return false;
        }

        return key_pressed[key_idx];
    }

    void on_key_down(int key_idx) {
        if (key_idx < 0 || key_idx >= key_pressed.length) {
            Log.d("PianOli::Piano", "This shouldn't happen: Sound out of range, key" + key_idx);
            return;
        }
        key_pressed[key_idx] = true;
        play_sound(key_idx);
    }

    void on_key_up(int key_idx) {
        if (key_idx < 0 || key_idx >= key_pressed.length) {
            Log.d("PianOli::Piano", "This shouldn't happen: Sound out of range, key" + key_idx);
            return;
        }

        key_pressed[key_idx] = false;
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
            return new Key(0, 0, 0, 0);
        }

        final int offset = keys_width - (keys_flat_width / 2);
        int x_i = (key_idx / 2) * keys_width + offset;
        return new Key(x_i, x_i + keys_flat_width, 0, keys_flats_height);
    }

    void selectSoundset(final Context context, String soundSetName) {
        if (KeySound != null) {
            KeySound.release();
        }

        KeySound = new SoundPool.Builder()
                .setMaxStreams(7)   // Play max N concurrent sounds
                .build();

        KeySoundIdx = new int[28];
        final AssetManager am = context.getAssets();
        try {
            KeySoundIdx[0] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n01.mp3"), 1);
            KeySoundIdx[1] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n02.mp3"), 1);
            KeySoundIdx[2] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n03.mp3"), 1);
            KeySoundIdx[3] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n04.mp3"), 1);
            KeySoundIdx[4] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n05.mp3"), 1);
            KeySoundIdx[5] = KeySound.load(context, R.raw.no_note, 1);
            KeySoundIdx[6] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n06.mp3"), 1);
            KeySoundIdx[7] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n07.mp3"), 1);
            KeySoundIdx[8] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n08.mp3"), 1);
            KeySoundIdx[9] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n09.mp3"), 1);
            KeySoundIdx[10] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n10.mp3"), 1);
            KeySoundIdx[11] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n11.mp3"), 1);
            KeySoundIdx[12] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n12.mp3"), 1);
            KeySoundIdx[13] = KeySound.load(context, R.raw.no_note, 1);

            KeySoundIdx[14] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n13.mp3"), 1);
            KeySoundIdx[15] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n14.mp3"), 1);
            KeySoundIdx[16] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n15.mp3"), 1);
            KeySoundIdx[17] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n16.mp3"), 1);
            KeySoundIdx[18] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n17.mp3"), 1);
            KeySoundIdx[19] = KeySound.load(context, R.raw.no_note, 1);
            KeySoundIdx[20] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n18.mp3"), 1);
            KeySoundIdx[21] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n19.mp3"), 1);
            KeySoundIdx[22] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n20.mp3"), 1);
            KeySoundIdx[23] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n21.mp3"), 1);
            KeySoundIdx[24] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n22.mp3"), 1);
            KeySoundIdx[25] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n23.mp3"), 1);
            KeySoundIdx[26] = KeySound.load(am.openFd("sounds/" + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/n24.mp3"), 1);
            KeySoundIdx[27] = KeySound.load(context, R.raw.no_note, 1);
        } catch (IOException e) {
            Log.d("PianOli::Piano", "Failed to load sounds");
            e.printStackTrace();
        }
    }

    private void play_sound(int key_idx) {
        if (key_idx < 0 || key_idx >= KeySoundIdx.length) {
            Log.d("PianOli::Piano", "This shouldn't happen: Sound out of range, key" + key_idx);
            return;
        }

        if (this.melody != null) {
            key_idx = this.melody.get(this.melody_idx++);
            if (this.melody_idx >= this.melody.size()) {
                this.melody_idx = 0;
            }
        }

        KeySound.play(KeySoundIdx[key_idx], 1, 1, 1, 0, 1f);
    }

    static class Key {
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
}
