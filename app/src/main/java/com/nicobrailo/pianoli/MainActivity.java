package com.nicobrailo.pianoli;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set a bunch of flags to make it full screen. If any of the features are
        // not available, ignore them

        try {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {}

        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception e) {}

        try {
            getSupportActionBar().hide();
        } catch (Exception e) {}

        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } catch (Exception e) {}

        setContentView(R.layout.activity_main);

        try {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        } catch (Exception e) {}

        lock_app();
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
}
