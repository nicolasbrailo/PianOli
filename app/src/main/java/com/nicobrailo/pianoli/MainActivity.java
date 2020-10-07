package com.nicobrailo.pianoli;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements AppConfigTrigger.AppConfigCallback {

    private PianoCanvas piano_canvas = null;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set a bunch of flags to make it full screen. If any of the features are
        // not available, ignore them

        try {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) { /* Ignore, the app can survive without fancy UI options */ }

        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception e) { /* Ignore, the app can survive without fancy UI options */ }

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } catch (Exception e) { /* Ignore, the app can survive without fancy UI options */ }

        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(view);

        piano_canvas = view.findViewById(R.id.piano_canvas);
        piano_canvas.setConfigRequestCallback(this);

        try {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOptions);
        } catch (Exception e) { /* Ignore, the app can survive without fancy UI options */ }

        lock_app();

        // Show list of available sounds in config screen
        final ArrayList<String> available_sound_sets = getAvailableSoundsets();
        final ListView sound_set_list_view = view.findViewById(R.id.sound_set_list);
        // Carefully hand-crafted width, otherwise control width = screen width
        sound_set_list_view.getLayoutParams().width = 750;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, available_sound_sets);
        sound_set_list_view.setAdapter(adapter);

        sound_set_list_view.setOnItemClickListener((parent, view1, position, id) -> {
            final String selected_soundset = available_sound_sets.get(position);
            Log.i("PianOli::Activity", "Selected " + selected_soundset);
            piano_canvas.selectSoundset(getApplicationContext(), selected_soundset);

            final String msg = "Sound set selected: " + selected_soundset;
            Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
            toast.show();
        });
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

        if (lst.length == 0) {
            final String msg = "No sounds found, the keyboard won't work!";
            Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
            toast.show();

            Log.d("PianOli::Activity", "Sound assets not available: piano will have no sound!");
        }

        return new ArrayList<>(Arrays.asList(lst));
    }

    void lock_app() {
        startLockTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        lock_app();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        lock_app();
    }

    public void onClick_CloseConfig(View view) {
        findViewById(R.id.piano_canvas).bringToFront();
        findViewById(R.id.config_layout).setVisibility(View.GONE);
    }

    public void onClick_QuitApp(View view) {
        stopLockTask();
        this.startActivity(new Intent(this, MainActivity.class));
        moveTaskToBack(true);
    }

    public void onClick_About(View view) {
        final String url = Uri.parse(getString(R.string.app_url)).buildUpon().build().toString();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        stopLockTask();
        startActivity(intent);
    }

    @Override
    public void onConfigOpenRequested() {
        findViewById(R.id.config_layout).bringToFront();
        findViewById(R.id.config_layout).setVisibility(View.VISIBLE);
    }

    @Override
    public void onShowConfigTooltip() {
        final String msg = "Press and hold config icons on black keys to exit.";
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        toast.show();
    }
}
