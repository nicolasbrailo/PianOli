package com.nicobrailo.pianoli;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private boolean exit_traps_enabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set fullscreen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        this.setContentView(new DrawingCanvas(this));
    }

    public void onClick_ExitTrapsToggle(View v) {
        exit_traps_enabled = ! exit_traps_enabled;
        if (exit_traps_enabled) {
            ((Button)v).setText("Exit blocked");
        } else {
            ((Button)v).setText("Exit unblocked");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (exit_traps_enabled) {
            ActivityManager activityManager = (ActivityManager) getApplicationContext()
                    .getSystemService(Context.ACTIVITY_SERVICE);

            activityManager.moveTaskToFront(getTaskId(), 0);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Block back button
        if (exit_traps_enabled && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        return super.dispatchKeyEvent(event);
    }
}
