package com.nicobrailo.pianoli;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Preferences {

    private static final String TAG = "Preferences";
    private static final String DEFAULT_SOUNDSET = "piano";
    private final static String PREF_SELECTED_SOUND_SET = "selectedSoundSet";
    private final static String PREF_SELECTED_MELODIES = "selectedMelodies";
    private final static String PREF_ENABLE_MELODIES = "enableMelodies";

    /**
     * If none are selected, then we play all melodies.
     * This is counter intuitive from a user perspective ("Why is it playing all the
     * melodies when I deselected them all!"), however it is probably more counter intuitive
     * than the alternative which is "Why did it not play any melodies when I selected
     * 'enable melodies'?").
     */
    public static List<Melody> selectedMelodies(Context context) {
        final String[] defaultMelodies = context.getResources().getStringArray(R.array.default_selected_melodies);
        Set<String> defaultMelodiesSet = new HashSet<>();
        Collections.addAll(defaultMelodiesSet, defaultMelodies);
        final Set<String> selectedMelodies = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(PREF_SELECTED_MELODIES, defaultMelodiesSet);

        final ArrayList<Melody> melodies = new ArrayList<>(selectedMelodies.size());
        for (Melody melody : SingleSongMelody.all) {
            if (selectedMelodies.isEmpty() || selectedMelodies.contains(melody.id())) {
                melodies.add(melody);
            }
        }
        return melodies;
    }

    public static boolean areMelodiesEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_ENABLE_MELODIES, false);
    }

    /**
     * The sound set is the name of the folder in assets/sounds/soundset_[NAME]
     * (note that the soundset_ prefix is stripped from the directory name before being recorded here).
     */
    public static String selectedSoundSet(Context context) {
        final String soundsetName = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SELECTED_SOUND_SET, DEFAULT_SOUNDSET);

        // Should never return null, but the linter has picked up that getString() can strictly speaking
        // return null if a null was saved into preferences in the past, so may as well be defensive here.
        if (soundsetName == null) {
            Log.w(TAG, "Hmm, we have a null soundset for some unknown reason. Defaulting to \"" + DEFAULT_SOUNDSET + "\".");
            setSelectedSoundSet(context, DEFAULT_SOUNDSET);
            return DEFAULT_SOUNDSET;
        }

        // When fixing issue #25, the preference was always prefixed with the directory name.
        // This will not play any sound, so lets take the liberty of updating the preference to the correct
        // format for them. This can be removed in the future if we like after most people will have migrated
        // to the newer version.
        if (soundsetName.startsWith(SettingsActivity.SOUNDSET_DIR_PREFIX)) {
            String updatedSoundsetName = soundsetName.substring(SettingsActivity.SOUNDSET_DIR_PREFIX.length());
            Log.i(TAG, "Migrating from existing soundset \"" + soundsetName + "\" to new format: \"" + updatedSoundsetName + "\"");
            setSelectedSoundSet(context, updatedSoundsetName);
            return updatedSoundsetName;
        }

        return soundsetName;
    }

    public static void setSelectedSoundSet(Context context, String soundSet) {
        Log.d(TAG, "Selecting soundset \"" + soundSet + "\"");
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SELECTED_SOUND_SET, soundSet)
                .apply();
    }

}
