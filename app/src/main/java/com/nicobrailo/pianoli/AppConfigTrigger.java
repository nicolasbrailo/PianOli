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


/**
 * Listens for, and defines, the "magic" key combination that unlocks the app, and opens the settings menu.
 *
 * <p>
 * The next key to touch is indicated by a "settings" gear-icon, with already-pressed keys showing a smaller version.
 * </p>
 * <p>
 * The "magic" combination is a random combination of multiple black keys, to be held simultaneously.
 * The amount of which is defined by {@link #CONFIG_TRIGGER_COUNT} (currently {@value #CONFIG_TRIGGER_COUNT}).
 * We've chosen a simultaneous-hold koy combo, rather than a serial sequence, because children in our target audience
 * lack the fine motor skills to achieve it (at least without accidentally triggering a reset by accidentally
 * brushing another key), but they <em>would</em> be able to trigger a serial sequence by playing "follow the gear".
 * </p>
 */
class AppConfigTrigger implements PianoListener {
    private static final float CONFIG_ICON_SIZE_TO_FLAT_KEY_RATIO = 0.5f;
    private static final float CONFIG_ICON_SIZE_TO_FLAT_KEY_RATIO_PRESSED = 0.4f;

    /** How many of the geared keys must be held before config opens */
    private static final int CONFIG_TRIGGER_COUNT = 2;

    /**
     * Candidate keys to receive a gear icon.
     *
     * <p>Currently a hardcoded set of the first </p>
     */
    private static final Set<Integer> BLACK_KEYS = new HashSet<>(Arrays.asList(1, 3, 7, 9, 11, 15));

    private final AppCompatActivity activity;

    /**
     * Current progress in the unlock sequence: all already-held config-keys.
     *
     * <p>
     * We need to track which keys are held, not just their amount, to<ol>
     *     <li>avoid re-selecting them as next candidate key</li>
     *     <li>draw icons on them.</li>
     * </ol>
     * </p>
     */
    private final Set<Integer> pressedConfigKeys = new HashSet<>();

    /**
     * @see #calculateNextExpectedKey()
     */
    private Integer nextExpectedKey;

    /**
     * Our "upstream", who knows enougjh about global app context to actually <em>do</em> stuff.
     */
    private AppConfigCallback cb = null;

    private boolean tooltip_shown = false;
    private final Drawable icon;

    AppConfigTrigger(AppCompatActivity activity) {
        nextExpectedKey = calculateNextExpectedKey();
        this.activity = activity;
        this.icon = ContextCompat.getDrawable(activity, R.drawable.ic_settings);
        if (this.icon == null) {
            Log.wtf("PianOliError", "Config icon doesn't exist");
        }
    }

    void setConfigRequestCallback(AppConfigCallback cb) {
        this.cb = cb;
    }

    /**
     * Chooses the next key that must be held to make progress in the sequence.
     *
     * <p>
     * Ensures already-held keys are not chosen again.
     * </p>
     */
    private Integer calculateNextExpectedKey() {
        Set<Integer> candidates = new HashSet<>(BLACK_KEYS);
        candidates.removeAll(pressedConfigKeys);

        // Since we cannot easily pick a random selection from a set directly,
        // (at least not at the low API-level we want to support)
        // iterate the set to a random depth and select that one.
        int i = (new Random()).nextInt(candidates.size());
        for (Integer nextKey : candidates) {
            i--;
            if (i <= 0) { return nextKey; }
        }

        Log.e("PianOliError", "No next config key possible");
        return -1;
    }

    /**
     * Resets all progress towards opening the config, back to zero.
     *
     * <p>
     * If any gear-keys were already held, a new expected key is randomly chosen from <em>non-held</em> keys.
     * This ensures any current touches lose their status as "progress".
     * </p>
     *
     * @see #pressedConfigKeys
     * @see #calculateNextExpectedKey()
     */
    private void reset() {
        // Only change expected keys if there was some progress to reset, otherwise this would select a
        // new NextExpectedKey and move the icon around whenever the user presses a key.
        if (!pressedConfigKeys.isEmpty()) {
            // Calculate next expectation *before* clearing pressedConfigKeys, to keep current touches
            // out of the candidate list. Otherwise, we could accidentally make an already-held key into a 'magic'
            // key, thereby granting the user unlock-progress without them doing anything to deserve it.
            nextExpectedKey = calculateNextExpectedKey();
        }

        pressedConfigKeys.clear();
    }

    private void showConfigDialogue() {
        final MediaPlayer snd = MediaPlayer.create(activity, R.raw.alert);
        snd.seekTo(0);
        snd.setVolume(100, 100);
        snd.start();
        snd.setOnCompletionListener(mediaPlayer -> snd.release());

        if (cb != null) {
            cb.requestConfig();
        }
    }

    @Override
    public void onKeyDown(int keyIdx) {
        if (keyIdx == nextExpectedKey) {
            // Hint the user at what to do next, if not already done.
            if (!tooltip_shown) {
                tooltip_shown = true;
                cb.showConfigTooltip();
            }

            // track user's progress in the unlock-sequence
            pressedConfigKeys.add(keyIdx);
            if (pressedConfigKeys.size() == CONFIG_TRIGGER_COUNT) {
                // Sequence complete!
                reset(); // clear the currently tracked state, for next time.
                showConfigDialogue(); // Open Sesame!
            } else {
                nextExpectedKey = calculateNextExpectedKey();
            }
        } else {
            // wrong key: force user/child to start from the beginning.
            reset();
        }
    }

    /**
     * Reset all unlock-sequence progress.
     *
     * <p>
     * Releasing *any* key means we are either<ul>
     *   <li>aborting our in-progress sequence (released key was a geared one), or</li>
     *   <li>another 'wrong' key used to be pressed and is now released</li>
     * </ul>
     * Either way, we want to force the user to start over, for touching a non-config key.
     * (A mistake an adult would have been able to avoid, but a child likely wouldn't).
     * </p>
     *
     * @param keyIdx unused for this purpose, all releases are equally 'mistaken'.
     */
    @Override
    public void onKeyUp(int keyIdx) {
        reset();
    }

    /**
     * Overlays gear icons onto the currently-held and next expected keys.
     */
    void drawGears(PianoCanvas pianoCanvas, Canvas androidCanvas) {
        int pressedSize = (int) (pianoCanvas.piano.get_keys_flat_width() * CONFIG_ICON_SIZE_TO_FLAT_KEY_RATIO_PRESSED);
        for (Integer cfgKey : pressedConfigKeys) {
            pianoCanvas.draw_icon_on_black_key(androidCanvas, icon, cfgKey, pressedSize, pressedSize);
        }

        int normalSize = (int) (pianoCanvas.piano.get_keys_flat_width() * CONFIG_ICON_SIZE_TO_FLAT_KEY_RATIO);
        pianoCanvas.draw_icon_on_black_key(androidCanvas, icon, nextExpectedKey, normalSize, normalSize);
    }

    /**
     * Decoupling interface, to keep Android-environment awareness out of this Trigger-class.
     *
     * <p>
     * Switching activities, and showing user UI feedback, require a level of global application awareness
     * that is out of place for this trigger-tracker.
     * Via this interface, we delegate our required actions to a higher-up that is allowed to have such
     * awareness.
     * </p>
     */
    public interface AppConfigCallback {
        /** Switch to the Config/Settings Activity */
        void requestConfig();

        /** Show a hint to the user on how to use the gear icons */
        void showConfigTooltip();
    }
}
