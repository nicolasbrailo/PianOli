package com.nicobrailo.pianoli;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    private Melody melody = null;

    Piano(final Context context, int screen_size_x, int screen_size_y, final String soundset) {
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

        if (Preferences.areMelodiesEnabled(context)) {
            this.melody = new MultipleSongsMelody(Preferences.selectedMelodies(context));
            this.melody.reset();
        }
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

    private static Map<String, Integer> note_to_key_idx = new HashMap<>();

    static {
        note_to_key_idx.put("C1", 0);
        note_to_key_idx.put("C#1", 1);
        note_to_key_idx.put("D1", 2);
        note_to_key_idx.put("D#1", 3);
        note_to_key_idx.put("E1", 4);

        note_to_key_idx.put("F1", 6);
        note_to_key_idx.put("F#1", 7);
        note_to_key_idx.put("G1", 8);
        note_to_key_idx.put("G#1", 9);
        note_to_key_idx.put("A1", 10);
        note_to_key_idx.put("A#1", 11);
        note_to_key_idx.put("B1", 12);

        note_to_key_idx.put("C2", 14);
        note_to_key_idx.put("C#2", 15);
        note_to_key_idx.put("D2", 16);
        note_to_key_idx.put("D#2", 17);
        note_to_key_idx.put("E2", 18);

        note_to_key_idx.put("F2", 20);
        note_to_key_idx.put("F#2", 21);
        note_to_key_idx.put("G2", 22);
        note_to_key_idx.put("G#2", 23);
        note_to_key_idx.put("A2", 24);
        note_to_key_idx.put("A#2", 25);
        note_to_key_idx.put("B2", 26);
    }

    int get_key_idx_from_note(String note) {

        Integer key_idx = note_to_key_idx.get(note);
        if (key_idx == null) {
            Log.w("PianOli::Piano", "Could not find a key corresponding to the note \"" + note + "\".");

            // 5 is designated as the special sound T.raw.no_note, so the app wont crash, but it wont
            // play a noise either.
            return 5;
        }

        return key_idx;
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
            if (!this.melody.hasNextNote()) {
                this.melody.reset();
            }

            String note = this.melody.nextNote();
            key_idx = get_key_idx_from_note(note);
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
