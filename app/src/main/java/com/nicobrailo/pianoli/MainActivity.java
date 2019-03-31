package com.nicobrailo.pianoli;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AppConfigTrigger.AppConfigCallback {

    @RequiresApi(api = Build.VERSION_CODES.N)
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

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } catch (Exception e) {}

        @SuppressLint("InflateParams")
        final View view = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(view);
        ((PianoCanvas) view.findViewById(R.id.piano_canvas)).setConfigRequestCallback(this);

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
