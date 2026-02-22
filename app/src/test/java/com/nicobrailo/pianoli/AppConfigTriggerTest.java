package com.nicobrailo.pianoli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTriggerTest {
    private SpyCallback spyCallback;
    private AppConfigTrigger trigger;

    private boolean automaticTrigger = false;
    private int scheduledTimerID = -1;

    @BeforeEach
    public void setup() {
        spyCallback = new SpyCallback();
        trigger = new AppConfigTrigger() {
            @Override
            protected void scheduleTimer(int delayMs, int timerID) {
                assertTrue(delayMs >= 0, "delay should not be negative");
                assertTrue(timerID >= 0, "timer ID must be positive");
                if (automaticTrigger) {
                    onTimer(timerID);
                } else {
                    scheduledTimerID = timerID;
                }
            }
        };
        trigger.setConfigRequestCallback(spyCallback);
    }

    @Test
    public void happyPathWorks() {
        automaticTrigger = false;
        for (int i = 0; i < AppConfigTrigger.CONFIG_TRIGGER_COUNT; i++) {
            assertEquals(0, spyCallback.triggerCount,
                    "Before reaching trigger limit, we should not yet trigger (i=" + i + ")");
            int nextExpectedKey = trigger.getNextExpectedKey();
            trigger.onKeyDown(nextExpectedKey);
        }
        assertEquals(0, spyCallback.triggerCount, "should not trigger yet");
        assertEquals(-1, trigger.getNextExpectedKey(), "while waiting for the timer, there should be no expected key to press");
        assertEquals(2, trigger.getPressedConfigKeys().size(), "while waiting for the timer, the pressed key should still be pressed");
        assertNotEquals(-1, scheduledTimerID, "there should be a timer scheduled");

        trigger.onTimer(scheduledTimerID);

        assertEquals(1, spyCallback.triggerCount,
                "after hitting the required amount of trigger keys, without mistakes and after waiting for timer, config should trigger.");

        assertTrue(trigger.getPressedConfigKeys().isEmpty(),
                "unlock should clear the pressed-state for the successful sequence.\n" +
                        "Our unlock instruction reminder counts key-ups of config keys, to track \"user frustration\".\n" +
                        "Thus, it is important that key-ups of <em>successful</em> unlocks (hopefully a non-frustrating event),\n" +
                        "clear state before they are accidentally counted.");
    }

    @Test
    public void timerCancelledOnKeyUp() {
        automaticTrigger = false;
        int lastKey = 0;
        for (int i = 0; i < AppConfigTrigger.CONFIG_TRIGGER_COUNT; i++) {
            assertEquals(0, spyCallback.triggerCount,
                    "Before reaching trigger limit, we should not yet trigger (i=" + i + ")");
            int nextExpectedKey = trigger.getNextExpectedKey();
            trigger.onKeyDown(nextExpectedKey);
            lastKey = nextExpectedKey;
        }

        assertEquals(0, spyCallback.triggerCount);
        trigger.onKeyUp(lastKey);
        assertEquals(0, spyCallback.triggerCount);
        trigger.onTimer(scheduledTimerID);
        assertEquals(0, spyCallback.triggerCount);
    }


    @Test
    public void timerCancelledOnKeyDown() {
        automaticTrigger = false;
        for (int i = 0; i < AppConfigTrigger.CONFIG_TRIGGER_COUNT; i++) {
            assertEquals(0, spyCallback.triggerCount,
                    "Before reaching trigger limit, we should not yet trigger (i=" + i + ")");
            int nextExpectedKey = trigger.getNextExpectedKey();
            trigger.onKeyDown(nextExpectedKey);
        }

        assertEquals(0, spyCallback.triggerCount);
        trigger.onKeyDown(2);// Not a black key, so it is not pressed down
        assertEquals(0, spyCallback.triggerCount);
        trigger.onTimer(scheduledTimerID);
        assertEquals(0, spyCallback.triggerCount);
    }

    @Test
    public void badKeyDownShouldCancelProgress() {
        automaticTrigger = true;
        for (int i = 0; i < 100; i++) {
            // make some correct progress
            int nextExpectedKey = trigger.getNextExpectedKey();
            trigger.onKeyDown(nextExpectedKey);
            assertTrue(trigger.getPressedConfigKeys().contains(nextExpectedKey),
                    "Hitting correct key should make progress");

            // "oops"
            trigger.onKeyDown(Integer.MIN_VALUE); // since any mistake should reset progress, test with an impossible mistake value
            assertTrue(trigger.getPressedConfigKeys().isEmpty(),
                    "a bad key-down should reset all progress");
            assertEquals(0, spyCallback.triggerCount,
                    "even after a gazillion (i="+i+") bad attempts, we should never trigger");
        }
    }

    @Test
    public void anyKeyUpShouldCancelProgress() {
        automaticTrigger = true;
        for (int i = 0; i < 50; i++) { // try with a LOT of keys, including some absurdly high ones.
            // make some correct progress
            int nextExpectedKey = trigger.getNextExpectedKey();
            trigger.onKeyDown(nextExpectedKey);
            assertTrue(trigger.getPressedConfigKeys().contains(nextExpectedKey),
                    "Hitting correct key should make progress");

            // "oops"
            trigger.onKeyUp(i);
            assertTrue(trigger.getPressedConfigKeys().isEmpty(),
                    "a bad key-release should reset all progress");
            assertEquals(0, spyCallback.triggerCount,
                    "even after a gazillion (i="+i+") bad attempts, we should never trigger");
        }
    }

    /**
     * When touching any key that <em>isn't</em> a config key, the next expected key
     * should remain constant, to avoid the gear icon twitching on every key-press.
     */
    @Test
    public void gearIconShouldntTwitch() {
        int originalConfigKey = trigger.getNextExpectedKey();
        int pianoSize = 50; // given our wonderfully decoupled interface, this is all the piano-mock we need :-D

        for (int i = 0; i < pianoSize; i++) {
            if (i == originalConfigKey) {
                continue; // skip the ONE key we're actually listening for
            }
            trigger.onKeyDown(i); // trigger all other keys
            assertEquals(originalConfigKey, trigger.getNextExpectedKey(),
                    "Expected key should change ONLY if the current expected key is triggered." +
                            "(to prevent distracting visual twitches of the icon under normal use)");
        }
    }


}
