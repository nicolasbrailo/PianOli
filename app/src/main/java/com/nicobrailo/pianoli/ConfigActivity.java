package com.nicobrailo.pianoli;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.TypedArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ConfigActivity extends AppCompatActivity {

    public static final String SOUNDSET_DIR_PREFIX = "soundset_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_config);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Show list of available sounds in config screen
        final ArrayList<String> available_sound_sets = getAvailableSoundsets();
        final ListView sound_set_list_view = findViewById(R.id.sound_set_list);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_single_choice, available_sound_sets);
        sound_set_list_view.setAdapter(adapter);

        final int selected_index = available_sound_sets.indexOf(Preferences.selectedSoundSet(this));
        sound_set_list_view.setItemChecked(selected_index, true);

        sound_set_list_view.setOnItemClickListener((parent, view1, position, id) -> {
            final String selected_soundset = available_sound_sets.get(position);
            Log.i("PianOli::Activity", "Selected " + selected_soundset);
            Preferences.setSelectedSoundSet(this, selected_soundset);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_config, menu);
        return true;
    }

    public static final int RESULT_QUIT = 1;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_quit_app) {
            setResult(RESULT_QUIT);
            finish();
            return true;
        }

        return false;
    }

    private ArrayList<String> getAvailableSoundsets() {
        // Show list of available sounds in config screen
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

    public void onClick_About(View view) {
        final String url = Uri.parse(getString(R.string.app_url)).buildUpon().build().toString();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        startActivity(intent);
    }

}
