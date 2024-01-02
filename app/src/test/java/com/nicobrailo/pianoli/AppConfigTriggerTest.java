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

        assertTrue(trigger.getPressedConfigKeys().isEmpty(),
                "unlock should clear the pressed-state for the successful sequence.\n" +
                        "Our unlock instruction reminder counts key-ups of config keys, to track \"user frustration\".\n" +
                        "Thus, it is important that key-ups of <em>successful</em> unlocks (hopefully a non-frustrating event),\n" +
                        "clear state before they are accidentally counted.");
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
