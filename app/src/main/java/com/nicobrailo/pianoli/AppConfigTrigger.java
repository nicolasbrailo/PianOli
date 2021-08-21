package com.nicobrailo.pianoli;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

class AppConfigTrigger {
    private static final float CONFIG_ICON_SIZE_TO_FLAT_KEY_RATIO = 0.5f;
    private static final float CONFIG_ICON_SIZE_TO_FLAT_KEY_RATIO_PRESSED = 0.4f;
    private static final int CONFIG_TRIGGER_COUNT = 2;
    private static final Set<Integer> BLACK_KEYS = new HashSet<>(Arrays.asList(1, 3, 7, 9, 11, 15));
    private final AppCompatActivity activity;
    private Set<Integer> pressedConfigKeys = new HashSet<>();
    private Integer nextKeyPress;
    private AppConfigCallback cb = null;
    private boolean tooltip_shown = false;
    private final Drawable icon;

    AppConfigTrigger(AppCompatActivity activity) {
        nextKeyPress = getNextExpectedKey();
        this.activity = activity;
        this.icon = ContextCompat.getDrawable(activity, R.drawable.ic_settings);
        if (this.icon == null) {
            Log.wtf("PianOliError", "Config icon doesn't exist");
        }
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
        // Only do an actual reset if there was some state to reset, otherwise this will select a
        // new NextExpectedKey and move the icon around whenever the user presses a key
        if (pressedConfigKeys.size() > 0) {
            nextKeyPress = getNextExpectedKey();
            pressedConfigKeys.clear();
        }
    }

    private void showConfigDialogue() {
        final MediaPlayer snd = MediaPlayer.create(activity, R.raw.alert);
        snd.seekTo(0);
        snd.setVolume(100, 100);
        snd.start();
        snd.setOnCompletionListener(mediaPlayer -> snd.release());

        if (cb != null) {
            cb.onConfigOpenRequested();
        }
    }

    void onKeyPress(int key_idx) {
        if (key_idx == nextKeyPress) {
            if (!tooltip_shown) {
                tooltip_shown = true;
                cb.onShowConfigTooltip();
            }

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
        } else {
            reset();
        }
    }

    void onPianoRedrawFinish(PianoCanvas piano, Canvas canvas) {
        int pressedSize = (int) (piano.piano.get_keys_flat_width() * CONFIG_ICON_SIZE_TO_FLAT_KEY_RATIO_PRESSED);
        for (Integer cfgKey : pressedConfigKeys) {
            piano.draw_icon_on_black_key(canvas, icon, cfgKey, pressedSize, pressedSize);
        }

        int normalSize = (int) (piano.piano.get_keys_flat_width() * CONFIG_ICON_SIZE_TO_FLAT_KEY_RATIO);
        piano.draw_icon_on_black_key(canvas, icon, nextKeyPress, normalSize, normalSize);
    }

    public interface AppConfigCallback {
        void onConfigOpenRequested();

        void onShowConfigTooltip();
    }
}
