package com.nicobrailo.pianoli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreferencesTest {
    @Test
    public void defaultThemeExists() {
        assertEquals(Theme.RAINBOW, Theme.fromPreference(Preferences.DEFAULT_THEME));
    }
}
