package com.nicobrailo.pianoli;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

public class Preferences {

    private static final String TAG = "Preferences";
    private static final String DEFAULT_SOUNDSET = "piano";
    private final static String PREF_SELECTED_SOUND_SET = "selectedSoundSet";

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
