package com.nicobrailo.pianoli;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.nicobrailo.pianoli.melodies.Melody;
import com.nicobrailo.pianoli.sound.SoundSet;

import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat {

    private List<String> availableSoundsets;

    public SettingsFragment() {
        // empty ctor may be required for fragments
        this.availableSoundsets = null;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        loadSounds();
        loadMelodies();
    }

    public void onAttach (@NonNull Context context) {
        super.onAttach(context);
        availableSoundsets = SoundSet.getAvailableSoundsets(context.getAssets());

        if (availableSoundsets.isEmpty()) {
            final String msg = "No sounds found, the keyboard won't work!";
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            toast.show();

            Log.d("PianOli::Activity", "Sound assets not available: piano will have no sound!");
        }
    }

    void loadMelodies() {
        MultiSelectListPreference melodies = findPreference("selectedMelodies");
        if (melodies != null) {

            boolean enabled = getPreferenceManager().getSharedPreferences().getBoolean("enableMelodies", false);
            melodies.setEnabled(enabled);

            findPreference("enableMelodies").setOnPreferenceChangeListener((preference, newValue) -> {
                melodies.setEnabled((Boolean)newValue);
                return true;
            });

            String[] melodyEntries = new String[Melody.all.length];
            String[] melodyEntryValues = new String[Melody.all.length];

            // Ideally we'd also call setDefaultValue() here too and pass a Set<String>
            // containing each melody. However, the system invokes the "persist default values"
            // before we get here, and thus it never gets respected. Instead, that is hardcoded
            // in a string-array and referenced directly in root_preferences.xml.

            for (int i = 0; i < Melody.all.length; i ++) {
                Melody melody = Melody.all[i];
                melodyEntryValues[i] = melody.getId();

                @SuppressLint("DiscouragedApi")
                int stringId = getResources().getIdentifier("melody_" + melody.getId(), "string", requireContext().getPackageName());
                melodyEntries[i] = stringId > 0 ? getString(stringId) : melody.getId();
            }

            melodies.setEntries(melodyEntries);
            melodies.setEntryValues(melodyEntryValues);
        }
    }

    void loadSounds() {
        ListPreference soundsets = findPreference("selectedSoundSet");
        if (soundsets != null) {
            String[] soundsetEntries = new String[availableSoundsets.size()];
            String[] soundsetEntryValues = new String[availableSoundsets.size()];
            for (int i = 0; i < availableSoundsets.size(); i ++) {
                String rawName = availableSoundsets.get(i);
                soundsetEntryValues[i] = rawName;

                String translationKey = SoundSet.addPrefix(rawName);

                @SuppressLint("DiscouragedApi")
                int stringId = getResources().getIdentifier(translationKey, "string", requireContext().getPackageName());
                soundsetEntries[i] = stringId > 0 ? getString(stringId) : rawName;
            }

            soundsets.setEntries(soundsetEntries);
            soundsets.setEntryValues(soundsetEntryValues);
        }
    }
}
