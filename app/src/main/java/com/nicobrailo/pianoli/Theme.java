package com.nicobrailo.pianoli;

import android.content.Context;
import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

public class Theme {

    private final int[] colours;
    private final int[] pressedColours;

    public static Theme fromPreferences(Context context) {
        String selectedTheme = Preferences.selectedTheme(context);
        switch (selectedTheme) {
            case "black_and_white":
                return BLACK_AND_WHITE;

            default:
                return BOOMWHACKER;
        }
    }

    private static final int[] BOOMWHACKER_COLORS = new int[] {
            Color.rgb(220, 0, 0),     // Red
            Color.rgb(255, 135, 0),   // Orange
            Color.rgb(255, 255, 0),   // Yellow
            Color.rgb(80, 220, 20),   // Light Green
            Color.rgb(0, 150, 150),   // Dark Green
            Color.rgb(95, 70, 165),   // Purple
            Color.rgb(213, 43, 149),  // Pink
    };

    /**
     * Note that light green, orange and yellow have higher lightness than other colours,
     * so adding just a little white doesn't have the desired effect.
     * That is why they have a larger proportion of white added in.
     */
    private static final int[] BOOMWHACKER_PRESSED_COLORS = new int[] {
        ColorUtils.blendARGB(BOOMWHACKER_COLORS[0], Color.WHITE, 0.5f),    // Red
        ColorUtils.blendARGB(BOOMWHACKER_COLORS[1], Color.WHITE, 0.6f),    // Orange
        ColorUtils.blendARGB(BOOMWHACKER_COLORS[2], Color.WHITE, 0.75f),   // Yellow
        ColorUtils.blendARGB(BOOMWHACKER_COLORS[3], Color.WHITE, 0.6f),    // Light Green
        ColorUtils.blendARGB(BOOMWHACKER_COLORS[4], Color.WHITE, 0.5f),    // Dark Green
        ColorUtils.blendARGB(BOOMWHACKER_COLORS[5], Color.WHITE, 0.5f),    // Purple
        ColorUtils.blendARGB(BOOMWHACKER_COLORS[6], Color.WHITE, 0.5f),    // Pink
    };

    private static final Theme BOOMWHACKER = new Theme(BOOMWHACKER_COLORS, BOOMWHACKER_PRESSED_COLORS);

    private static final Theme BLACK_AND_WHITE = new Theme(
            new int[] { Color.rgb(240, 240, 240) },
            new int[] { Color.rgb(200, 200, 200) }
    );

    private Theme(int[] colors, int[] pressedColors) {
        this.colours = colors;
        this.pressedColours = pressedColors;
    }

    public int getColorForKey(int keyIndex, boolean isPressed) {
        final int col_idx = (keyIndex / 2) % colours.length;
        return isPressed ? pressedColours[col_idx] : colours[col_idx];
    }

}
