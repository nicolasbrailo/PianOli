package com.nicobrailo.pianoli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class TooltipReminderTest {

    private SpyCallback spyCallback;
    private TestableTooltipReminder reminder;

    @BeforeEach
    public void setup() {
        spyCallback = new SpyCallback();
        reminder = new TestableTooltipReminder(spyCallback);
    }


    @Test
    public void firstTouchShouldTrigger() {
        reminder.registerFailedAttempt();
        assertEquals(1, spyCallback.toastCount,
                "First touch of a config key after init should show a reminder, to preserve old behaviour.");
    }

    @Test
    public void slowTouchesDontTrigger() {
        long slowInterval = TooltipReminder.COUNTING_WINDOW + 1;

        // get initial reminder-trigger out of the way
        reminder.registerFailedAttempt();

        for (int i = 0; i < 10; i++) {
            reminder.nextNow += slowInterval; // "time passes"
            reminder.registerFailedAttempt(); // user acts
            assertEquals(1, spyCallback.toastCount,
                    "slow hits of the config keys shouldn't trigger a reminder");
        }
    }

    @Test
    public void fastTouchesDoTrigger() {
        long fastInterval = TooltipReminder.COUNTING_WINDOW - 1;

        // get initial reminder-trigger out of the way
        reminder.registerFailedAttempt();

        // simulate nothing happening for a while, or only normal piano playing
        reminder.nextNow += TooltipReminder.COUNTING_WINDOW + 1;

        // prime the pump: attempts until just before we start triggering
        for (int i = 0; i < TooltipReminder.TRIGGER_COUNT; i++) {
            reminder.registerFailedAttempt();
        }
        assertEquals(1, spyCallback.toastCount,
                "after a long-ish time of inactivity, failed attempts should not immediately trigger " +
                        "(to avoid distraction on accidental presses).");

        for (int i = 1; i <= 10; i++) { // gotacha: one-based counting! (for easier loop counting, below
            reminder.nextNow += fastInterval; // "time passes", but not enough
            reminder.registerFailedAttempt(); // user acts
            assertEquals(1 + i, spyCallback.toastCount, // 1 base reminder, plus our loop count
                    "slow hits of the config keys shouldn't trigger a reminder");
        }
    }


    /** Testable version of {@link TooltipReminder}, with manipulable clock */
    static class TestableTooltipReminder extends TooltipReminder {
        /** fake time-value, for easier testing of time-related logic */
        public long nextNow = 0;

        public TestableTooltipReminder(AppConfigTrigger.AppConfigCallback cb) {
            super(cb);
        }

        /**
         * @return fake time, namely {@link #nextNow}
         */
        @Override
        long getNow() {
            return nextNow;
        }
    }
}
