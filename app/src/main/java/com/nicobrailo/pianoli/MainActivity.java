package com.nicobrailo.pianoli;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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

        setContentView(R.layout.activity_main);

        piano_canvas = findViewById(R.id.piano_canvas);
        piano_canvas.setConfigRequestCallback(this);
        piano_canvas.selectSoundset(this, Preferences.selectedSoundSet(this));

        try {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOptions);
        } catch (Exception e) { /* Ignore, the app can survive without fancy UI options */ }
    }

    void lock_app() {
        startLockTask();
    }

    void unlock_app() {
        stopLockTask();
    }

    @Override
    protected void onResume() {
        super.onResume();

        piano_canvas.selectSoundset(this, Preferences.selectedSoundSet(this));
        lock_app();
    }

    public void quitApp() {
        unlock_app();
        this.startActivity(new Intent(this, MainActivity.class));
        moveTaskToBack(true);
    }

    private static final int REQUEST_CONFIG = 1;

    @Override
    public void onConfigOpenRequested() {
        // If you've done the dance to press multiple specific buttons at once, no need to keep the screen locked.
        // It will be a minor inconvenience when returning from settings, because it will prompt the user again
        // to lock the app. However the expectation is that the options are not used very often, and the benefit
        // of having a settings screen work like a more typical Android app probably outweigh the negatives from a
        // child accidentally getting to the settings screen.
        unlock_app();

        startActivityForResult(new Intent(this, ConfigActivity.class), REQUEST_CONFIG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUEST_CONFIG) {
            if (resultCode == ConfigActivity.RESULT_QUIT) {
                quitApp();
            }

            // Don't need to lock the app if the user just hit the "Up" button in the action bar here again,
            // because onResume would have already been called.
        }
    }

    @Override
    public void onShowConfigTooltip() {
        final String msg = "Press and hold config icons on black keys to exit.";
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        toast.show();
    }
}
