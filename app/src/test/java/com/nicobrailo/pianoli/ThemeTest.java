package com.nicobrailo.pianoli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThemeTest {
    /**
     * Checks if theme values from historical versions of the app can be decoded.
     *
     * <p>
     * Tests against a hardcoded copy of {@link R.array#theme_entryValues}, in order to preserve historical values,
     * even if we remove some themes in the future.
     * </p>
     *
     * @see Theme#fromPreference(String)
     * @see R.array#theme_entryValues
     */
    @ParameterizedTest(name = "[{index}] pref {0} -> Theme.{1}")
    @CsvSource({
            "rainbow,RAINBOW",
            "pastel,PASTEL",
            "black_and_white,BLACK_AND_WHITE",
            "boomwhacker,BOOMWHACKER",
    })
    public void possiblyPersistedThemePreferencesWork(String persistedName, String expectedTheme) {
        // test the intended, productive mapping
        Theme decodedPref = Theme.fromPreference(persistedName);
        assertEquals(expectedTheme, decodedPref.name(),
                "mapping the possibly-persisted theme-preference '" + persistedName + "' yielded a surprising Theme-implementation: " + decodedPref);
    }

    /**
     * Checks if all currently implemented {@link Theme}s can be reached via their preference-identifier.
     *
     * <p>
     * This explicitly <em>does not</em> test if the app-UI will actually offer the theme in the settings UI.
     * That kind of android UI-dependant test is out of scope here.
     * </p>
     *
     * @see Theme#fromPreference(String)
     */
    @ParameterizedTest
    @EnumSource(Theme.class)
    public void fromPreferenceRoundTrip(Theme theme) {
        String prefName = theme.name().toLowerCase(Locale.ROOT); // Root-locale should be sufficient for java identifiers.

        assertEquals(theme, Theme.fromPreference(prefName),
                "Theme not roundtrippable via preferences; see Theme.fromPreference()");
    }

    /**
     * In case we ever mess up our preferences-handling, it is better to forget the user's color preference,
     * than it is to crash the app.
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "non-existing-test-theme")
    public void unknownPreferencesShouldFallback(String brokenPref) {
        assertEquals(Theme.RAINBOW, Theme.fromPreference(brokenPref));
    }
}
