package com.nicobrailo.pianoli;

import android.content.Context;
import android.preference.PreferenceManager;

public class Preferences {

    private static final String DEFAULT_SOUNDSET = "soundset_piano";
    private final static String PREF_SELECTED_SOUND_SET = "selectedSoundSet";

    /**
     * The sound set is the name of the folder in assets/sounds/soundset_[NAME]
     * (note that the soundset_ prefix is stripped from the directory name before being recorded here).
     */
    public static String selectedSoundSet(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SELECTED_SOUND_SET, DEFAULT_SOUNDSET);
    }

    public static void setSelectedSoundSet(Context context, String soundSet) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SELECTED_SOUND_SET, soundSet)
                .apply();
    }

}
