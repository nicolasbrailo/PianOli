package com.nicobrailo.pianoli;

import java.util.concurrent.TimeUnit;

/**
 * Since our config entry sequence is intentionally quite difficult, we should <em>regularly</em>
 * provide our users with instruction reminders, but not <em>too</em> often, so as not to distract any
 * children playing.
 *
 * <p>
 *
 * </p>
 */
public class TooltipReminder {
    /**
     * Time window (milliseconds) in which repeated unsuccessful config attempts will lead to a reminder tooltip.
     *
     *
     * @see #TRIGGER_COUNT
     */
    public static final long COUNTING_WINDOW = TimeUnit.SECONDS.toMillis(5);

    /**
     * How often a user must unsuccessfully attempt to open the config, before we show a helpful reminder.
     *
     * @see #COUNTING_WINDOW
     */
    public static final int TRIGGER_COUNT = 5;
    /**
     * Start of currently running tooltip reminder time window (epoch millis).
     *
     * @see #COUNTING_WINDOW
     */
    private long lastAttempt;
    /**
     * How many unsuccessful attempts the user made so far. A.K.A. the user's "AAARGH counter".
     */
    private int frustration;
    private final AppConfigTrigger.AppConfigCallback cb;

    public TooltipReminder(AppConfigTrigger.AppConfigCallback cb) {
        this.cb = cb;

        // Initialise to magic values, that are guaranteed to trigger the very first time
        // This guarantees the user is reminded at least once every startup.
        this.frustration = TRIGGER_COUNT - 1; // = one step before overflowing
        this.lastAttempt = Long.MAX_VALUE;    // = guaranteed to bring our comparisn against 'now' before our timeout.
    }

    /**
     * The user tried, but failed, to enter the config.
     *
     * <p>
     * Increases the need for a reminder.
     * </p>
     */
    public void registerFailedAttempt() {
        long now = getNow();

        if (now - lastAttempt < COUNTING_WINDOW) {
            // repeated attempt within detection window
            frustration++;
        } else {
            // enough time has passed that frustration is less likely than accidental child bap-bap-bap-ing
            frustration = 0; // reset
        }
        lastAttempt = now;

        if (frustration >= TRIGGER_COUNT) {
            cb.showConfigTooltip();

            // reminder was shown, user should no longer be frustrated
            // reset the counter to ensure we don't immediately spam reminders on the next hit.
            frustration = 0;
        }
    }

    /**
     * Test seam: override to inject fake time for testing.
     *
     * @return current time in milliseconds
     * @see System#currentTimeMillis()
     */
    long getNow() {
        return System.currentTimeMillis();
    }
}
