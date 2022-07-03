package com.nicobrailo.pianoli;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    public static final String SOUNDSET_DIR_PREFIX = "soundset_";

    public static final int RESULT_QUIT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment(getAvailableSoundsets()))
                    .commit();
        }

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private ArrayList<String> getAvailableSoundsets() {
        AssetManager am = getApplicationContext().getAssets();
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
            if (s.startsWith(SOUNDSET_DIR_PREFIX)) {
                // User display should be the asset name without the prefix
                filtList.add(s.substring(SOUNDSET_DIR_PREFIX.length()));
            }
        }

        if (filtList.size() == 0) {
            final String msg = "No sounds found, the keyboard won't work!";
            Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
            toast.show();

            Log.d("PianOli::Activity", "Sound assets not available: piano will have no sound!");
        }

        return filtList;
    }

    private void onQuit() {
        setResult(RESULT_QUIT);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_quit_app) {
            onQuit();
            return true;
        }

        return false;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private String[] soundsetEntries;
        private String[] soundsetEntryValues;

        public SettingsFragment(List<String> availableSoundsets) {
            this.soundsetEntryValues = availableSoundsets.toArray(new String[] {});
            this.soundsetEntries = new String[availableSoundsets.size()];
            this.soundsetEntryValues = new String[availableSoundsets.size()];
            for (int i = 0; i < availableSoundsets.size(); i ++) {
                this.soundsetEntryValues[i] = availableSoundsets.get(i);
                this.soundsetEntries[i] = availableSoundsets.get(i);
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            ListPreference soundsets = findPreference("selectedSoundSet");
            if (soundsets != null) {
                soundsets.setEntries(soundsetEntries);
                soundsets.setEntryValues(soundsetEntryValues);
            }
        }

    }
}
