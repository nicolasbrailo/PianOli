package com.nicobrailo.pianoli;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class AppConfigActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appconfig);
    }

    public void onClick_CloseConfig(View view) {
        this.startActivity(new Intent(this, MainActivity.class));
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
}
