package com.nicobrailo.pianoli;

import android.util.Log;
import androidx.annotation.NonNull;

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
abstract class AppConfigTrigger implements PianoListener {
    /** How many of the geared keys must be held before config opens */
    public static final int CONFIG_TRIGGER_COUNT = 2;

    /** For how many milliseconds must the trigger key combination be held to activate. */
    public static final int TRIGGER_DELAY_MS = 300;

    /**
     * Candidate keys to receive a gear icon.
     *
     * <p>Currently a hardcoded set of keys</p>
     */
    private static final Set<Integer> BLACK_KEYS = new HashSet<>(Arrays.asList(1, 3, 7, 9, 11, 15));

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
     * The total amount of currently pressed keys.
     * We do not allow the unlock sequence to start if there are any other pressed keys.
     */
    private int pressedKeyCount = 0;

    /**
     * User frustration tracker: how badly are they failing to open the config?
     *
     * @see #cb
     * @see #setConfigRequestCallback(AppConfigCallback)
     */
    private TooltipReminder tooltipReminder;

    /**
     * @see #calculateNextExpectedKey()
     */
    private int nextExpectedKey;

    /**
     * Our "upstream", who knows enough about global app context to actually <em>do</em> stuff.
     *
     * @see #tooltipReminder
     * @see #setConfigRequestCallback(AppConfigCallback)
     */
    private AppConfigCallback cb = null;

    /**
     * Represents an timer ID that does not exist.
     */
    private static final int NO_TIMER_ID = -1;

    /**
     * Which timer are we waiting for right now to open the config?
     * This is set only when all trigger keys have been pressed and the timer has not fired yet.
     * If a key is pressed or released while we are waiting for the timer to finish,
     * this field will be reset and no config will open.
     */
    private int pendingTimerID = NO_TIMER_ID;

    AppConfigTrigger() {
        nextExpectedKey = calculateNextExpectedKey();
    }

    /**
     * Dependency injection for context-handling stuff: switching to settings activity, and showing toasts.
     *
     * <p>
     * Since this callback is <em>required</em> for this trigger to do anything at all, it would have been preferable
     * to require this is provided in the constructor. Alas, the way we initialise our upstream <code>PianoCanvas</code>
     * via XML definition precludes this.
     * </p>
     */
    void setConfigRequestCallback(@NonNull AppConfigCallback cb) {
        this.cb = cb;
        this.tooltipReminder = new TooltipReminder(cb);
    }

    /**
     * @return set of currently-held config keys (defensively copied).
     */
    public Set<Integer> getPressedConfigKeys() {
        return new HashSet<>(pressedConfigKeys);
    }

    /**
     * @return currently expected next key in the sequence (without changing it). May be -1 if there is no expected key.
     * @see #calculateNextExpectedKey();
     */
    public int getNextExpectedKey() {
        return nextExpectedKey;
    }

    /**
     * Chooses the next key that must be held to make progress in the sequence.
     *
     * <p>
     * Ensures already-held keys are not chosen again.
     * </p>
     */
    private int calculateNextExpectedKey() {
        Set<Integer> candidates = new HashSet<>(BLACK_KEYS);
        candidates.removeAll(pressedConfigKeys);

        if (candidates.isEmpty()) {
            Log.e("PianOliError", "No next config key possible");
            return -1;
        }

        // Since we cannot easily pick a random selection from a set directly,
        // (at least not at the low API-level we want to support)
        // iterate the set to a random depth and select that one.
        int i = (new Random()).nextInt(candidates.size());
        for (Integer nextKey : candidates) {
            i--;
            if (i <= 0) { return nextKey; }
        }

        // Unreachable due to way candidates.size is upper bound for loop count,
        // but that's too complicated for the compiler to figure out.
        // (it can't see through the Random.nextInt() ).
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
    void reset() {
        // Only change expected keys if there was some progress to reset, otherwise this would select a
        // new NextExpectedKey and move the icon around whenever the user presses a key.
        if (!pressedConfigKeys.isEmpty()) {
            // Calculate next expectation *before* clearing pressedConfigKeys, to keep current touches
            // out of the candidate list. Otherwise, we could accidentally make an already-held key into a 'magic'
            // key, thereby granting the user unlock-progress without them doing anything to deserve it.
            nextExpectedKey = calculateNextExpectedKey();
        }

        pressedConfigKeys.clear();
        pendingTimerID = NO_TIMER_ID;
    }

    @Override
    public void onKeyDown(int keyIdx) {
        boolean validSequence = keyIdx == nextExpectedKey && pendingTimerID == NO_TIMER_ID;
        if (validSequence && pressedConfigKeys.isEmpty() && pressedKeyCount != 0) {
            // If this is the first key of the sequence, but there are already some pressed keys,
            // do not start the sequence.
            validSequence = false;
        }

        pressedKeyCount++;
        if (validSequence) {
            // track user's progress in the unlock-sequence
            pressedConfigKeys.add(keyIdx);
            if (pressedConfigKeys.size() == CONFIG_TRIGGER_COUNT) {
                nextExpectedKey = -1;
                pendingTimerID = (int)(System.nanoTime() & 0x7FFF_FFFF);// Arbitrary positive semi-unique ID
                scheduleTimer(TRIGGER_DELAY_MS, pendingTimerID);
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
        pressedKeyCount--;

        if (pressedConfigKeys.contains(keyIdx)) {
            // The released key was part of an in-progress unlock-sequence
            // (completed sequence would have invoked reset, thus clearing this set, before we get here)
            tooltipReminder.registerFailedAttempt();
        }
        reset();
    }

    /**
     * Reset the pressed key count.
     * This is done to protect us should the user somehow manage to create unbalanced key-down/up
     * calls. In that case it would not be possible to unlock the config an the user would get stuck.
     */
    public void resetPressedKeys() {
        pressedKeyCount = 0;
    }

    /**
     * Requests that the implementation calls {@link #onTimer(int)}
     * after some delay.
     * @param delayMs how many milliseconds should the delay be
     * @param timerID should be passed to {@link #onTimer(int)} as a parameter, to identify the timer
     */
    protected abstract void scheduleTimer(int delayMs, int timerID);

    protected void onTimer(int timerID) {
        if (timerID == pendingTimerID) {
            pendingTimerID = NO_TIMER_ID;
            // Sequence complete!
            reset(); // clear it so it's no longer counted as in-progress.
            // Open Sesame!
            cb.requestConfig();
        }
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
