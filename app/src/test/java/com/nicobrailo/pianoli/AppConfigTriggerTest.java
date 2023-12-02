package com.nicobrailo.pianoli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTriggerTest {
    private SpyCallback spyCallback;
    private AppConfigTrigger trigger;

    @BeforeEach
    public void setup() {
        spyCallback = new SpyCallback();
        trigger = new AppConfigTrigger();
        trigger.setConfigRequestCallback(spyCallback);
    }

    @Test
    public void happyPathWorks() {
        for (int i = 0; i < AppConfigTrigger.CONFIG_TRIGGER_COUNT; i++) {
            assertEquals(0, spyCallback.triggerCount,
                    "Before reaching trigger limit, we should not yet trigger (i=" + i + ")");
            int nextExpectedKey = trigger.getNextExpectedKey();
            trigger.onKeyDown(nextExpectedKey);
        }
        assertEquals(1, spyCallback.triggerCount,
                "after hitting the required amount of trigger keys, without mistakes, config should trigger.");
    }

    @Test
    public void badKeyDownShouldCancelProgress() {
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

    static class SpyCallback implements AppConfigTrigger.AppConfigCallback {
        int triggerCount = 0;
        int toastCount = 0;

        @Override
        public void requestConfig() {
            triggerCount++;
        }

        @Override
        public void showConfigTooltip() {
            toastCount++;
        }
    }
}
