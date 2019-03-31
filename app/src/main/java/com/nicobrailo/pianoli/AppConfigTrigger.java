package com.nicobrailo.pianoli;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

class AppConfigTrigger {
    public interface AppConfigCallback {
        void onConfigOpenRequested();
    }

    private static final int CONFIG_TRIGGER_COUNT = 2;
    private static final Set<Integer> BLACK_KEYS = new HashSet<>(Arrays.asList(1, 3, 7, 9, 11, 15));
    private final AppCompatActivity activity;
    private Set<Integer> pressedConfigKeys = new HashSet<>();
    private Integer nextKeyPress;
    private AppConfigCallback cb;

    AppConfigTrigger(AppCompatActivity activity) {
        nextKeyPress = getNextExpectedKey();
        this.activity = activity;
        this.cb = null;
    }

    void setConfigRequestCallback(AppConfigCallback cb) {
        this.cb = cb;
    }

    private Integer getNextExpectedKey() {
        Set<Integer> nextKeyOptions = new HashSet<>(BLACK_KEYS);
        nextKeyOptions.removeAll(pressedConfigKeys);
        int next_key_i = (new Random()).nextInt(nextKeyOptions.size());

        for (Integer nextKey : nextKeyOptions) {
            next_key_i = next_key_i - 1;
            if (next_key_i <= 0) return nextKey;
        }

        Log.e("PianOliError", "No next config key possible");
        return -1;
    }

    private void reset() {
        nextKeyPress = getNextExpectedKey();
        pressedConfigKeys.clear();
    }

    private void showConfigDialogue() {
        final MediaPlayer snd = MediaPlayer.create(activity, R.raw.alert);
        snd.seekTo(0);
        snd.setVolume(100, 100);
        snd.start();
        snd.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                snd.release();
            }
        });

        if (cb != null) {
            cb.onConfigOpenRequested();
        }
    }

    void onKeyPress(int key_idx) {
        if (key_idx == nextKeyPress) {
            pressedConfigKeys.add(key_idx);
            if (pressedConfigKeys.size() == CONFIG_TRIGGER_COUNT) {
                reset();
                showConfigDialogue();
            } else {
                nextKeyPress = getNextExpectedKey();
            }
        }
    }

    void onKeyUp(int key_idx) {
        if (pressedConfigKeys.contains(key_idx)) {
            reset();
        }
    }

    void onPianoRedrawFinish(PianoCanvas piano, Canvas canvas, Context ctx) {
        Drawable icon = ContextCompat.getDrawable(ctx, android.R.drawable.ic_menu_preferences);

        for (Integer cfgKey : pressedConfigKeys) {
            piano.draw_icon_on_black_key(canvas, icon, cfgKey, 70, 70);
        }

        piano.draw_icon_on_black_key(canvas, icon, nextKeyPress, 70, 70);
    }
}
