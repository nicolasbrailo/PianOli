package com.nicobrailo.pianoli;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.SoundPool;
import android.util.Log;

import com.nicobrailo.pianoli.melodies.MelodyPlayer;
import com.nicobrailo.pianoli.melodies.MultipleSongsMelodyPlayer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

/**
 * Backing model / state of our virtual piano keyboard.
 *
 * <p>
 * Handles geometry, coordinate conversion and current state.
 * </p>
 *
 * @see PianoCanvas
 */
class Piano implements PianoListener {
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
    /** handle to our android-provided sound mixer, that mixes multiple simultaneous tones */
    private static SoundPool KeySound = null;
    /** Resource handles to the mp3 samples of our currently active soundset, one for each note */
    private int[] KeySoundIdx = new int[0]; // HACK: init empty to prevent NPE in tests

    /** For song-auto-player, the state-tracker of where we are in our (selection of) melodie(s). */
    private MelodyPlayer melody = null;

    /**
     * Construct a partially initialised (geometry only) Piano model.
     *
     * <p>
     * Resource- and Preference-depedendent init is deferred to {@link #init(Context, String)}, for better testability.
     * </p>
     *
     * @param screen_size_x the long dimension of the screen (keys are side-by-side along this axis)
     * @param screen_size_y the short dimension of the screen.
     *
     * @see #init(Context, String)
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
    }

    Piano init(final Context context, final String soundset) {
        selectSoundset(context, soundset);

        if (Preferences.areMelodiesEnabled(context)) {
            this.melody = new MultipleSongsMelodyPlayer(Preferences.selectedMelodies(context));
            this.melody.reset();
        }

        return this;
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

    @Override
    public void onKeyDown(int keyIdx) {
        if (isOutOfRange(keyIdx)) {
            Log.d("PianOli::Piano", "This shouldn't happen: Key-Down out of range, key" + keyIdx);
            return;
        }
        key_pressed[keyIdx] = true;
        play_sound(keyIdx);
    }

    @Override
    public void onKeyUp(int keyIdx) {
        if (isOutOfRange(keyIdx)) {
            Log.d("PianOli::Piano", "This shouldn't happen: Key-Up out of range, key" + keyIdx);
            return;
        }

        key_pressed[keyIdx] = false;
    }

    private boolean isOutOfRange(int key_idx) {
        return key_idx < 0 || key_idx >= key_pressed.length;
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

    // Developer note: Sound-playing deals with android-resources, which makes me think
    //     it would be better of in PianoCanvas instead (so Piano becomes pure "geometry")
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
            int loadedNoNote = KeySound.load(context, R.raw.no_note, 1);

            KeySoundIdx[0]  = loadNoteFd(am, soundSetName, 1);
            KeySoundIdx[1]  = loadNoteFd(am, soundSetName, 2);
            KeySoundIdx[2]  = loadNoteFd(am, soundSetName, 3);
            KeySoundIdx[3]  = loadNoteFd(am, soundSetName, 4);
            KeySoundIdx[4]  = loadNoteFd(am, soundSetName, 5);
            KeySoundIdx[5]  = loadedNoNote;
            KeySoundIdx[6]  = loadNoteFd(am, soundSetName, 6);
            KeySoundIdx[7]  = loadNoteFd(am, soundSetName, 7);
            KeySoundIdx[8]  = loadNoteFd(am, soundSetName, 8);
            KeySoundIdx[9]  = loadNoteFd(am, soundSetName, 9);
            KeySoundIdx[10] = loadNoteFd(am, soundSetName, 10);
            KeySoundIdx[11] = loadNoteFd(am, soundSetName, 11);
            KeySoundIdx[12] = loadNoteFd(am, soundSetName, 12);
            KeySoundIdx[13] = loadedNoNote;

            KeySoundIdx[14] = loadNoteFd(am, soundSetName, 13);
            KeySoundIdx[15] = loadNoteFd(am, soundSetName, 14);
            KeySoundIdx[16] = loadNoteFd(am, soundSetName, 15);
            KeySoundIdx[17] = loadNoteFd(am, soundSetName, 16);
            KeySoundIdx[18] = loadNoteFd(am, soundSetName, 17);
            KeySoundIdx[19] = loadedNoNote;
            KeySoundIdx[20] = loadNoteFd(am, soundSetName, 18);
            KeySoundIdx[21] = loadNoteFd(am, soundSetName, 19);
            KeySoundIdx[22] = loadNoteFd(am, soundSetName, 20);
            KeySoundIdx[23] = loadNoteFd(am, soundSetName, 21);
            KeySoundIdx[24] = loadNoteFd(am, soundSetName, 22);
            KeySoundIdx[25] = loadNoteFd(am, soundSetName, 23);
            KeySoundIdx[26] = loadNoteFd(am, soundSetName, 24);
            KeySoundIdx[27] = loadedNoNote;
        } catch (IOException e) {
            Log.d("PianOli::Piano", "Failed to load sounds");
            e.printStackTrace();
        }
    }

    private static int loadNoteFd(AssetManager am, String soundSetName, int noteNum) throws IOException {
        return KeySound.load(am.openFd("sounds/"
                + SettingsActivity.SOUNDSET_DIR_PREFIX + soundSetName + "/"
                + String.format(Locale.ROOT, "n%02d.mp3", noteNum) // root locale OK for number-formatting.
        ), 1);
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

            key_idx = this.melody.nextNote();
        }

        KeySound.play(KeySoundIdx[key_idx], 1, 1, 1, 0, 1f);
    }
}
