package com.nicobrailo.pianoli;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Locale;

/**
 * Handle for the currently slected soundset, enabling the rest of the app to easily play notes.
 *
 * <p>
 * This is intended to be a "throw-away" object. When the selected instrument changes, {@link #close()} the current
 * object, and replace it with a new instance.
 * </p>
 */
public class SoundSet implements AutoCloseable {
    /**
     * The amount of samples/notes in each sound-set.
     *
     * <p>
     * Slightly inflated, since it also reserves slots for the non-existant black/flat keys.
     * This keeps the sample indices here in sync with {@link Piano}'s key-indices.
     * </p>
     */
    public static final int SOUNDSET_SAMPLES_SIZE = 28;

    /**
     * prefix for soundset stuff, both of asset-folders containing instrument samples and translation string keys.
     *
     * <p>
     * <ol>
     *      <li>See resources folder <code>app/srx/main/assets/sounds/</code></li>
     *      <li>Translation keys in <code>app/res/values../strings.xml</code></li>
     * </ol>
     * </p>
     */
    public static final String PREFIX = "soundset_";

    /**
     * Handle to our android-provided sound mixer, that mixes multiple simultaneous tones
     */
    private final SoundPool pool;

    /**
     * Resource handles to the mp3 samples of our currently active soundset, one for each note
     */
    private final int[] samples;

    public SoundSet(final Context context, String soundSetName) {
        pool = new SoundPool.Builder()
                .setMaxStreams(7)   // Play max N concurrent sounds
                .build();

        samples = new int[SOUNDSET_SAMPLES_SIZE];
        final AssetManager am = context.getAssets();
        try {
            int loadedNoNote = pool.load(context, R.raw.no_note, 1);

            samples[0]  = loadNoteFd(am, pool, soundSetName, 1);
            samples[1]  = loadNoteFd(am, pool, soundSetName, 2);
            samples[2]  = loadNoteFd(am, pool, soundSetName, 3);
            samples[3]  = loadNoteFd(am, pool, soundSetName, 4);
            samples[4]  = loadNoteFd(am, pool, soundSetName, 5);
            samples[5]  = loadedNoNote;
            samples[6]  = loadNoteFd(am, pool, soundSetName, 6);
            samples[7]  = loadNoteFd(am, pool, soundSetName, 7);
            samples[8]  = loadNoteFd(am, pool, soundSetName, 8);
            samples[9]  = loadNoteFd(am, pool, soundSetName, 9);
            samples[10] = loadNoteFd(am, pool, soundSetName, 10);
            samples[11] = loadNoteFd(am, pool, soundSetName, 11);
            samples[12] = loadNoteFd(am, pool, soundSetName, 12);
            samples[13] = loadedNoNote;

            samples[14] = loadNoteFd(am, pool, soundSetName, 13);
            samples[15] = loadNoteFd(am, pool, soundSetName, 14);
            samples[16] = loadNoteFd(am, pool, soundSetName, 15);
            samples[17] = loadNoteFd(am, pool, soundSetName, 16);
            samples[18] = loadNoteFd(am, pool, soundSetName, 17);
            samples[19] = loadedNoNote;
            samples[20] = loadNoteFd(am, pool, soundSetName, 18);
            samples[21] = loadNoteFd(am, pool, soundSetName, 19);
            samples[22] = loadNoteFd(am, pool, soundSetName, 20);
            samples[23] = loadNoteFd(am, pool, soundSetName, 21);
            samples[24] = loadNoteFd(am, pool, soundSetName, 22);
            samples[25] = loadNoteFd(am, pool, soundSetName, 23);
            samples[26] = loadNoteFd(am, pool, soundSetName, 24);
            samples[27] = loadedNoNote;
        } catch (IOException e) {
            Log.d("PianOli::Piano", "Failed to load sounds");
            e.printStackTrace();
        }
    }

    /**
     * Small helper to deduplicate sample-loading
     */
    private static int loadNoteFd(AssetManager am, SoundPool pool, String soundSetName, int noteNum) throws IOException {
        String assetFolder = "sounds/" + addPrefix(soundSetName) + "/";
        String fileName = String.format(Locale.ROOT, "n%02d.mp3", noteNum); // root locale OK for number-formatting.
        return pool.load(am.openFd(assetFolder + fileName), 1);
    }

    @Override
    public void close() {
        pool.release();
    }

    /**
     * Plays the sound sample associated with note <code>keyIdx</code>.
     *
     * @param keyIdx note to play. Range-checked, and does nothing (except log the problem) if the key is out of range.
     * @see #SOUNDSET_SAMPLES_SIZE
     */
    void playNote(int keyIdx) {
        if (keyIdx < 0 || keyIdx >= SOUNDSET_SAMPLES_SIZE) {
            Log.d("PianOli::SoundSet", "This shouldn't happen: Sound out of range: " + keyIdx);
            return;
        }

        pool.play(samples[keyIdx], 1, 1, 1, 0, 1f);
    }

    /**
     * Given a user-palatable soundset name, returns the system-prefixed version.
     *
     * <p>
     * Won't double-prefix
     * </p>
     *
     * @see #PREFIX
     */
    public static String addPrefix(String soundSetName) {
        // Prevent double-prefixing
        if (soundSetName.startsWith(PREFIX)) {
            return soundSetName;
        }

        return PREFIX + soundSetName;
    }

    /**
     * Given a system-prefixed soundset name, return the user-palatable version.
     */
    public static String stripPrefix(String soundSetName) {
        // Don't strip if there is no prefix
        if (!soundSetName.startsWith(PREFIX)) {
            return soundSetName;
        }

        return soundSetName.substring(PREFIX.length());
    }

    public static List<String> getAvailableSoundsets(AssetManager am) {
        try {
            ArrayList<String> soundsetDirs = new ArrayList<>();
            String[] allDirs = am.list("sounds");
            for (final String d : allDirs) {
                if (d.startsWith(PREFIX)) {
                    // User display should be the asset name without the prefix
                    soundsetDirs.add(stripPrefix(d));
                }
            }
            return soundsetDirs;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
