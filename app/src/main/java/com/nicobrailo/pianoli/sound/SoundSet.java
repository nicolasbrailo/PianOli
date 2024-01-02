package com.nicobrailo.pianoli.sound;

import android.content.res.AssetManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link SoundSet}s represent PianOli's capability to make musical noise in response to keypresses.
 *
 * This interface
 */
public interface SoundSet {
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
    String PREFIX = "soundset_";

    /**
     * Given a user-palatable soundset name, returns the system-prefixed version.
     *
     * <p>
     * Won't double-prefix
     * </p>
     *
     * @see #PREFIX
     */
    static String addPrefix(String soundSetName) {
        // Prevent double-prefixing
        if (soundSetName.startsWith(PREFIX)) {
            return soundSetName;
        }

        return PREFIX + soundSetName;
    }

    /**
     * Given a system-prefixed soundset name, return the user-palatable version.
     */
    static String stripPrefix(String soundSetName) {
        // Don't strip if there is no prefix
        if (!soundSetName.startsWith(PREFIX)) {
            return soundSetName;
        }

        return soundSetName.substring(PREFIX.length());
    }

    static List<String> getAvailableSoundsets(AssetManager am) {
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

    void playNote(int keyIdx);
}
