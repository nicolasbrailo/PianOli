package com.nicobrailo.pianoli;


import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat {

    private List<String> availableSoundsets;

    public SettingsFragment() {
        // empty ctor may be required for fragments
        this.availableSoundsets = null;
    }

    @Override
    public void onCreatePreferences(Bundle savedzInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        loadSounds();
    }

    public void onAttach (@NonNull Context context) {
        super.onAttach(context);
        this.availableSoundsets = getAvailableSoundsets(context);
        loadSounds();
    }

    private ArrayList<String> getAvailableSoundsets(Context context) {
        AssetManager am = context.getAssets();
        String[] lst = null;
        try {
            lst = am.list("sounds");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (lst == null) {
            lst = new String[0];
        }

        ArrayList<String> filtList = new ArrayList<>();
        for (final String s : lst) {
            if (s.startsWith(SettingsActivity.SOUNDSET_DIR_PREFIX)) {
                // User display should be the asset name without the prefix
                filtList.add(s.substring(SettingsActivity.SOUNDSET_DIR_PREFIX.length()));
            }
        }

        if (filtList.size() == 0) {
            final String msg = "No sounds found, the keyboard won't work!";
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            toast.show();

            Log.d("PianOli::Activity", "Sound assets not available: piano will have no sound!");
        }

        return filtList;
    }

    void loadSounds() {
        ListPreference soundsets = findPreference("selectedSoundSet");
        if (soundsets != null) {
            String[] soundsetEntries = new String[availableSoundsets.size()];
            String[] soundsetEntryValues = new String[availableSoundsets.size()];
            for (int i = 0; i < availableSoundsets.size(); i ++) {
                soundsetEntryValues[i] = availableSoundsets.get(i);

                String name = SettingsActivity.SOUNDSET_DIR_PREFIX + availableSoundsets.get(i);
                int stringId = getResources().getIdentifier(name, "string", requireContext().getPackageName());
                soundsetEntries[i] = stringId > 0 ? getString(stringId) : availableSoundsets.get(i);
            }

            soundsets.setEntries(soundsetEntries);
            soundsets.setEntryValues(soundsetEntryValues);
        }
    }
}
