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

        // Set fullscreen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        lock_app();
    }

    void lock_app() {
        startLockTask();
    }

    void unlock_app() {
        stopLockTask();
    }

    final Handler exit_taps_reset_handler = new Handler();
    final Runnable exit_taps_reseter = new Runnable() {
        @Override
        public void run() {
            exit_locks_reset();
        }
    };

    final int EXIT_TAPS = 3;
    final int EXIT_TAP_RESET_DELAY_MS = 2500;
    int exit_tap_count = 0;

    public void onClick_ExitRequest(View v) {
        // Move right/left to ensure it's not an accidental tap
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(v.getLayoutParams());
        p.addRule((exit_tap_count % 2 == 0) ?
                    RelativeLayout.ALIGN_PARENT_END : RelativeLayout.ALIGN_PARENT_START);
        v.setLayoutParams(p);

        // Notify the user the taps left
        final int taps_left = EXIT_TAPS - exit_tap_count;
        ((Button)v).setText(String.format(getString(R.string.exit_taps), taps_left));

        // Quit if taps reached
        if (exit_tap_count == EXIT_TAPS) {
            unlock_app();
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        } else {
            exit_tap_count += 1;

            // Reset the exit counter
            exit_taps_reset_handler.removeCallbacks(exit_taps_reseter);
            exit_taps_reset_handler.postDelayed(exit_taps_reseter, EXIT_TAP_RESET_DELAY_MS);
        }
    }

    private void exit_locks_reset() {
        exit_tap_count = 0;

        final View v = findViewById(R.id.btn_exit_request);
        ((Button)v).setText(R.string.exit);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(v.getLayoutParams());
        p.addRule(RelativeLayout.ALIGN_PARENT_START);
        v.setLayoutParams(p);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exit_taps_reset_handler.removeCallbacks(exit_taps_reseter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        exit_locks_reset();
        lock_app();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        exit_locks_reset();
        lock_app();
    }
}
