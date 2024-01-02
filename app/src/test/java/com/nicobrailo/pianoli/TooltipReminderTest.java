package com.nicobrailo.pianoli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.nicobrailo.pianoli.TooltipReminder.COUNTING_WINDOW;
import static com.nicobrailo.pianoli.TooltipReminder.TRIGGER_COUNT;
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
                "First touch after init should show a reminder, to preserve old behaviour.");
    }

    @Test
    public void slowTouchesDontTrigger() {
        long slowInterval = COUNTING_WINDOW + 1;

        // get initial reminder-trigger out of the way
        reminder.registerFailedAttempt();

        for (int i = 0; i < 10; i++) {
            reminder.nextNow += slowInterval; // "time passes"
            reminder.registerFailedAttempt(); // user acts
            assertEquals(1, spyCallback.toastCount,
                    "slow hits shouldn't trigger a reminder");
        }
    }

    @Test
    public void fastTouchesDoTrigger() {
        long fastInterval = COUNTING_WINDOW - 1;

        // get initial reminder-trigger out of the way
        reminder.registerFailedAttempt();

        // simulate nothing happening for a while, or only normal piano playing
        reminder.nextNow += COUNTING_WINDOW + 1;

        // prime the pump: attempts until just before we start triggering
        for (int i = 0; i < TRIGGER_COUNT; i++) {
            reminder.nextNow += fastInterval; // "time passes", but not enough
            reminder.registerFailedAttempt();
        }
        assertEquals(1, spyCallback.toastCount,
                "after a long-ish time of inactivity, failed attempts should not immediately trigger " +
                        "(to avoid distraction on accidental presses).");

        reminder.nextNow += fastInterval; // "time passes", but not enough
        reminder.registerFailedAttempt(); // user acts
        assertEquals(2, spyCallback.toastCount,
                "Fast hits should trigger a reminder");
    }

    @Test
    public void reminderShouldntSpam() {
        reminder.registerFailedAttempt(); // get initial reminder-trigger out of the way

        // gotcha: one-based loop-counting; so that expected-counts become easier.
        for (int repeat = 1; repeat < 5; repeat++) {
            // first few triggers shouldn't spam
            for (int i = 1; i < TRIGGER_COUNT; i++) {
                reminder.registerFailedAttempt();
                assertEquals(repeat, spyCallback.toastCount,
                        "Shouldn't re-toast immediately, so we don't spam; previous trigger should have reset counter."
                                + " repeat=" + repeat
                                + " i=" + i);
            }
            // at trigger count: should trigger
            reminder.registerFailedAttempt();
            assertEquals(1 + repeat, spyCallback.toastCount,
                    "a triggered reminder should reset our counter, so we don't spam toasts on repeated attempts"
                            + " repeat=" + repeat);

            // continue loop with a (hopefully) fresh window
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
