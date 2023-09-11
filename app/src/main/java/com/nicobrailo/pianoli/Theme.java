package com.nicobrailo.pianoli;

import android.content.Context;
import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

public class Theme {

    private final KeyColour[] colours;

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
     * Note that light green, orange and yellow have higher lightness than other colours,
     * so adding just a little white doesn't have the desired effect.
     * That is why they have a larger proportion of white added in.
     */
    private static final Theme BOOMWHACKER = new Theme(
            new KeyColour[] {
                    KeyColour.createLighterWhenPressed(Color.rgb(220, 0, 0), 0.5f),     // Red
                    KeyColour.createLighterWhenPressed(Color.rgb(255, 135, 0), 0.6f),   // Orange
                    KeyColour.createLighterWhenPressed(Color.rgb(255, 255, 0), 0.75f),   // Yellow
                    KeyColour.createLighterWhenPressed(Color.rgb(80, 220, 20), 0.6f),   // Light Green
                    KeyColour.createLighterWhenPressed(Color.rgb(0, 150, 150), 0.5f),   // Dark Green
                    KeyColour.createLighterWhenPressed(Color.rgb(95, 70, 165), 0.5f),   // Purple
                    KeyColour.createLighterWhenPressed(Color.rgb(213, 43, 149), 0.5f),  // Pink
            }
    );

    private static final Theme PASTEL = new Theme(
            new KeyColour[] {
                    // https://colorbrewer2.org/#type=qualitative&scheme=Pastel1&n=8
                    KeyColour.createLighterWhenPressed(0xfffbb4ae, 0.5f),
                    KeyColour.createLighterWhenPressed(0xffb3cde3, 0.5f),
                    KeyColour.createLighterWhenPressed(0xffccebc5, 0.5f),
                    KeyColour.createLighterWhenPressed(0xffdecbe4, 0.5f),
                    KeyColour.createLighterWhenPressed(0xfffed9a6, 0.5f),
                    KeyColour.createLighterWhenPressed(0xffffffcc, 0.5f),
                    KeyColour.createLighterWhenPressed(0xffe5d8bd, 0.5f),
            }
    );

    private static final Theme RAINBOW = new Theme(
            new KeyColour[] {
                    KeyColour.createLighterWhenPressed(0xff001caf, 0.5f), // darkblue
                    KeyColour.createLighterWhenPressed(0xff0099ff, 0.5f), // lightblue
                    KeyColour.createLighterWhenPressed(0xff63c624, 0.5f), // darkgreen
                    KeyColour.createLighterWhenPressed(0xffbde53d, 0.5f), // lightgreen
                    KeyColour.createLighterWhenPressed(0xfffcc000, 0.5f), // yellow
                    KeyColour.createLighterWhenPressed(0xffff810a, 0.5f), // lightorange
                    KeyColour.createLighterWhenPressed(0xffff5616, 0.5f), // darkorange
                    KeyColour.createLighterWhenPressed(0xffd51016, 0.5f), // red
            }
    );

    private static final Theme BLACK_AND_WHITE = new Theme(
            new KeyColour[] {
                    new KeyColour(
                            Color.rgb(240, 240, 240),
                            Color.rgb(200, 200, 200)
                    )
            }
    );

    private Theme(KeyColour[] colors) {
        this.colours = colors;
    }

    public int getColorForKey(int keyIndex, boolean isPressed) {
        final int col_idx = (keyIndex / 2) % colours.length;
        final KeyColour key = colours[col_idx];
        return isPressed ? key.pressedColour : key.colour;
    }

    private static class KeyColour {
        public final int colour;
        public final int pressedColour;

        public KeyColour(int colour, int pressedColour) {
            this.colour = colour;
            this.pressedColour = pressedColour;
        }

        public static KeyColour createLighterWhenPressed(int colour, float blendWhiteFactor) {
            return new KeyColour(colour, ColorUtils.blendARGB(colour, Color.WHITE, blendWhiteFactor));
        }
    }

}
