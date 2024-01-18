package com.nicobrailo.pianoli;

import android.content.Context;
import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

public class Theme {
    public static final String PREFIX = "theme_";

    private final KeyColor[] colors;

    public static Theme fromPreferences(Context context) {
        String selectedTheme = Preferences.selectedTheme(context);
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

    /**
     * Note that light green, orange and yellow have higher lightness than other colors,
     * so adding just a little white doesn't have the desired effect.
     * That is why they have a larger proportion of white added in.
     */
    private static final Theme BOOMWHACKER = new Theme(
            new KeyColor[] {
                    KeyColor.createLighterWhenPressed(Color.rgb(220, 0, 0), 0.5f),     // Red
                    KeyColor.createLighterWhenPressed(Color.rgb(255, 135, 0), 0.6f),   // Orange
                    KeyColor.createLighterWhenPressed(Color.rgb(255, 255, 0), 0.75f),   // Yellow
                    KeyColor.createLighterWhenPressed(Color.rgb(80, 220, 20), 0.6f),   // Light Green
                    KeyColor.createLighterWhenPressed(Color.rgb(0, 150, 150), 0.5f),   // Dark Green
                    KeyColor.createLighterWhenPressed(Color.rgb(95, 70, 165), 0.5f),   // Purple
                    KeyColor.createLighterWhenPressed(Color.rgb(213, 43, 149), 0.5f),  // Pink
            }
    );

    private static final Theme PASTEL = new Theme(
            new KeyColor[] {
                    // https://colorbrewer2.org/#type=qualitative&scheme=Pastel1&n=8
                    KeyColor.createLighterWhenPressed(0xfffbb4ae, 0.5f),
                    KeyColor.createLighterWhenPressed(0xffb3cde3, 0.5f),
                    KeyColor.createLighterWhenPressed(0xffccebc5, 0.5f),
                    KeyColor.createLighterWhenPressed(0xffdecbe4, 0.5f),
                    KeyColor.createLighterWhenPressed(0xfffed9a6, 0.5f),
                    KeyColor.createLighterWhenPressed(0xffffffcc, 0.5f),
                    KeyColor.createLighterWhenPressed(0xffe5d8bd, 0.5f),
            }
    );

    private static final Theme RAINBOW = new Theme(
            new KeyColor[] {
                    KeyColor.createLighterWhenPressed(0xff001caf, 0.5f), // darkblue
                    KeyColor.createLighterWhenPressed(0xff0099ff, 0.5f), // lightblue
                    KeyColor.createLighterWhenPressed(0xff63c624, 0.5f), // darkgreen
                    KeyColor.createLighterWhenPressed(0xffbde53d, 0.5f), // lightgreen
                    KeyColor.createLighterWhenPressed(0xfffcc000, 0.5f), // yellow
                    KeyColor.createLighterWhenPressed(0xffff810a, 0.5f), // lightorange
                    KeyColor.createLighterWhenPressed(0xffff5616, 0.5f), // darkorange
                    KeyColor.createLighterWhenPressed(0xffd51016, 0.5f), // red
            }
    );

    private static final Theme BLACK_AND_WHITE = new Theme(
            new KeyColor[] {
                    new KeyColor(
                            Color.rgb(240, 240, 240),
                            Color.rgb(200, 200, 200)
                    )
            }
    );

    private Theme(KeyColor[] colors) {
        this.colors = colors;
    }

    public int getColorForKey(int keyIndex, boolean isPressed) {
        final int col_idx = (keyIndex / 2) % colors.length;
        final KeyColor color = colors[col_idx];
        return isPressed ? color.pressed : color.normal;
    }

    private static class KeyColor {
        public final int normal;
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
