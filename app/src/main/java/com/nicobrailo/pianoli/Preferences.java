package com.nicobrailo.pianoli;

import android.content.Context;
import android.preference.PreferenceManager;

public class Preferences {

    private static final String DEFAULT_SOUNDSET = "piano";
    private final static String PREF_SELECTED_SOUND_SET = "selectedSoundSet";

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
