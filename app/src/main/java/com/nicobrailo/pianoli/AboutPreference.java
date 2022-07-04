package com.nicobrailo.pianoli;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class AboutPreference extends Preference {
    public AboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setLayoutResource(R.layout.preference_about);
    }

    public AboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setLayoutResource(R.layout.preference_about);
    }

    public AboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutResource(R.layout.preference_about);
    }

    public AboutPreference(Context context) {
        super(context);

        setLayoutResource(R.layout.preference_about);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchWebsite();
            }
        });
    }

    private void launchWebsite() {
        final String url = Uri.parse(getContext().getString(R.string.app_url)).buildUpon().build().toString();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        getContext().startActivity(intent);
    }
}
