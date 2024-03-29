package com.nicobrailo.pianoli;

import android.graphics.Color;
import androidx.core.graphics.ColorUtils;


/**
 * Switchable key-colouring decisions for {@link PianoCanvas}.
 *
 * <p>
 * Whenever {@link PianoCanvas} renders a key, it asks the current <code>Theme</code>-variant for the paint colour.
 * This allows us to switch palettes via preferences.
 * </p>
 *
 * @see Preferences#selectedTheme(android.content.Context)
 * @see PianoCanvas#drawKey(android.graphics.Canvas, int)
 */
public enum Theme {
    /**
     * Boomwhackers are colour-coded pipes that produce a (tuned) note when hit.
     * Their popularity in educational circles cemented their colour-to-note mapping as a de-facto international standard.
     *
     * <p>
     * See also: <a href="https://en.wikipedia.org/wiki/Boomwhacker">wikipedia: Boomwhacker</a>, and
     * <a href="https://boomwhackers.com/">boomwhackers.com</a>.
     * </p>
     *
     *
     * <p>
     * Note that light green, orange and yellow have higher lightness than other colors,
     * so adding just a little white doesn't have the desired effect.
     * That is why they have a larger proportion of white added in.
     * </p>
     */
    BOOMWHACKER(new KeyColor[] {
        KeyColor.createLighterWhenPressed(Color.rgb(220, 0, 0), 0.5f),     // Red
                KeyColor.createLighterWhenPressed(Color.rgb(255, 135, 0), 0.6f),   // Orange
                KeyColor.createLighterWhenPressed(Color.rgb(255, 255, 0), 0.75f),   // Yellow
                KeyColor.createLighterWhenPressed(Color.rgb(80, 220, 20), 0.6f),   // Light Green
                KeyColor.createLighterWhenPressed(Color.rgb(0, 150, 150), 0.5f),   // Dark Green
                KeyColor.createLighterWhenPressed(Color.rgb(95, 70, 165), 0.5f),   // Purple
                KeyColor.createLighterWhenPressed(Color.rgb(213, 43, 149), 0.5f),  // Pink
    }),

    /**
     * Soft pastel tones, derived from <a href="https://colorbrewer2.org/#type=qualitative&scheme=Pastel1&n=8">Colorbrewer2.org: Pastel</a>.
     */
    PASTEL(new KeyColor[] {
        KeyColor.createLighterWhenPressed(0xfffbb4ae, 0.5f), // dark pink
                KeyColor.createLighterWhenPressed(0xffb3cde3, 0.5f), // powder blue
                KeyColor.createLighterWhenPressed(0xffccebc5, 0.5f), // pistachio green
                KeyColor.createLighterWhenPressed(0xffdecbe4, 0.5f), // lavender
                KeyColor.createLighterWhenPressed(0xfffed9a6, 0.5f), // orange
                KeyColor.createLighterWhenPressed(0xffffffcc, 0.5f), // pale yellow
                KeyColor.createLighterWhenPressed(0xffe5d8bd, 0.5f), // light pink
    }),

    /**
     * All the colours of the rainbow, C1 dark blue, C2 red, then looping back to blue.
     */
    RAINBOW(new KeyColor[] {
        KeyColor.createLighterWhenPressed(0xff001caf, 0.5f), // darkblue
                KeyColor.createLighterWhenPressed(0xff0099ff, 0.5f), // lightblue
                KeyColor.createLighterWhenPressed(0xff63c624, 0.5f), // darkgreen
                KeyColor.createLighterWhenPressed(0xffbde53d, 0.5f), // lightgreen
                KeyColor.createLighterWhenPressed(0xfffcc000, 0.5f), // yellow
                KeyColor.createLighterWhenPressed(0xffff810a, 0.5f), // lightorange
                KeyColor.createLighterWhenPressed(0xffff5616, 0.5f), // darkorange
                KeyColor.createLighterWhenPressed(0xffd51016, 0.5f), // red
    }),

    /**
     * "classic" Ivory and Black.
     */
    BLACK_AND_WHITE(new KeyColor[] {
        new KeyColor( // white, lighter
                Color.rgb(240, 240, 240), // normal: slightly muted white;
                Color.rgb(200, 200, 200)  // darker gray when pressed
        )
    }); // note that the black flat keys are implicit and hardcoded for all themes at the moment.

    /**
     * Prefix for preferences-values and translation strings.
     *
     * <p>
     * Often used implicitly, so don't forget to do full-text searches across the project when changing this.
     * </p>
     */
    public static final String PREFIX = "theme_";

    /**
     * The sequence of colors to render; repeats from the start if there are more keys than array entries.
     */
    private final KeyColor[] colors;

    public static Theme fromPreference(String selectedTheme) {
        // defensive programming: if we ever mess up our preferences handling, it's better to fall back to default,
        // than to crash the app.
        if (selectedTheme == null) {
            return RAINBOW;
        }

        switch (selectedTheme) {
            case "black_and_white":
                return BLACK_AND_WHITE;

            case "pastel":
                return PASTEL;

            case "boomwhacker":
                return BOOMWHACKER;

            default:
                return RAINBOW;
        }
    }



    Theme(KeyColor[] colors) {
        this.colors = colors;
    }

    public int getColorForKey(int keyIndex, boolean isPressed) {
        if ((keyIndex & 1) == 1) { // odd index = black/flat/small key
            return isPressed ? Color.GRAY : 0xFF333333; // hardcoded "black" for now, but theme-able in the future.
        }

        final int col_idx = (keyIndex / 2) % colors.length; // divide by two to skip 'flat'/black keys at odd positions.
        final KeyColor color = colors[col_idx];
        return isPressed ? color.pressed : color.normal;
    }

    /**
     * The render-colours for a big piano key: {@link #normal} and {@link #pressed}.
     *
     * <p>
     * Note that this only applies to big keys, the 'flat' keys are always black.
     * </p>
     */
    private static class KeyColor {
        /** The normal rendering color, when the key is inactive */
        public final int normal;

        /**
         * The pressed/touched color of a key.
         *
         * <p>
         * Not automatically derived from {@link #normal}, because different hues need different amounts of
         * real lightening for the same amount of subjective lightening.
         * </p>
         *
         * @see #createLighterWhenPressed(int, float)
         */
        public final int pressed;

        public KeyColor(int normal, int pressed) {
            this.normal = normal;
            this.pressed = pressed;
        }

        public static KeyColor createLighterWhenPressed(int color, float blendWhiteFactor) {
            return new KeyColor(color, ColorUtils.blendARGB(color, Color.WHITE, blendWhiteFactor));
        }
    }
}
