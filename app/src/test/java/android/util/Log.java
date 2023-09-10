package android.util;

/**
 * Extremely hacky, dumb test-double of Android-Logging, which we use throughout the app
 *
 * <p>
 * When test-compiling, this will shadow the default (not-mocked-exception-throwing) implementation
 * provided by the Android Gradle Plugin.<br>
 * This is a low-tech way of avoiding a full-featured mocking dependency like Powermock or Mockito.
 * </p>
 * <p>
 * See also: Android Developer Guide: <a href="https://developer.android.com/training/testing/local-tests#mocking-dependencies">Build local unit tests</a><br>
 * Thank you <a href="https://stackoverflow.com/a/46793567">StackOverflow answer 46793567</a>.
 * </p>
 *
 * @noinspection unused
 */
public class Log {
    public static int d(String tag, String msg) {
        System.out.println("DEBUG: " + tag + ": " + msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        System.out.println("INFO: " + tag + ": " + msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        System.out.println("WARN: " + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        System.out.println("ERROR: " + tag + ": " + msg);
        return 0;
    }

    // add other methods if required...
}
