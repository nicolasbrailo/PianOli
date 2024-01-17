package android.graphics;

/**
 * Minimal test double for colour handling, so we can classload the {@link com.nicobrailo.pianoli.Theme} enum.
 *
 * <p>
 * When test-compiling, this will shadow the default (not-mocked-exception-throwing) implementation
 * provided by the Android Gradle Plugin.<br>
 * This is a low-tech way of avoiding a full-featured mocking dependency like Powermock or Mockito.
 * </p>
 */
public class Color {
    /**
     * Needed for {@link com.nicobrailo.pianoli.Theme#BLACK_AND_WHITE}
     * @noinspection unused
     */
    public static int rgb(int red, int green, int blue) {
        return 0;
    }

    /**
     * Used internally in {@link androidx.core.graphics.ColorUtils}, which is used by {@link com.nicobrailo.pianoli.Theme.KeyColor#createLighterWhenPressed(int, float)}
     * @noinspection unused
     */
    public static int alpha(int a) {
        return 0;
    }

    /**
     * Used internally in {@link androidx.core.graphics.ColorUtils}, which is used by {@link com.nicobrailo.pianoli.Theme.KeyColor#createLighterWhenPressed(int, float)}
     * @noinspection unused
     */
    public static int red(int a) {
        return 0;
    }

    /**
     * Used internally in {@link androidx.core.graphics.ColorUtils}, which is used by {@link com.nicobrailo.pianoli.Theme.KeyColor#createLighterWhenPressed(int, float)}
     * @noinspection unused
     */
    public static int green(int a) {
        return 0;
    }

    /**
     * Used internally in {@link androidx.core.graphics.ColorUtils}, which is used by {@link com.nicobrailo.pianoli.Theme.KeyColor#createLighterWhenPressed(int, float)}
     * @noinspection unused
     */
    public static int blue(int a) {
        return 0;
    }

    /**
     * Used internally in {@link androidx.core.graphics.ColorUtils}, which is used by {@link com.nicobrailo.pianoli.Theme.KeyColor#createLighterWhenPressed(int, float)}
     * @noinspection unused
     */
    public static int argb(int a, int r, int g, int b) {
        return 0;
    }
}
