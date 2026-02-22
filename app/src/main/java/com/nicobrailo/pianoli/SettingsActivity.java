package com.nicobrailo.pianoli;

import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {
    public SettingsActivity() {
        // empty ctor may be required for fragments
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_scroll), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // No top padding, already consumed by the toolbar
            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            return insets;
        });
    }

    private void onQuit() {
        // Really don't want to go back to the main activity after quiting, so a normal
        // finish() is no good (https://stackoverflow.com/questions/17719634/how-to-exit-an-android-app-programmatically).
        // If we return to the main activity, it will prompt to lock the screen again, as if
        // it was just launched.
        finishAffinity();
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
}
