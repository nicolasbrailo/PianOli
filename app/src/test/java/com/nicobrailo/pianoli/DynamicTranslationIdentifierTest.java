package com.nicobrailo.pianoli;

import android.content.Context;
import android.content.res.AssetManager;
import com.nicobrailo.pianoli.melodies.Melody;
import com.nicobrailo.pianoli.sound.SoundSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nicobrailo.pianoli.AssertionsExt.*;

/**
 * Consistency checks for dynamically-accessed entities: do they have translation-strings?
 *
 * <p>There are a few entities in PianOli that are derived dynamically at runtime.
 * The IDE/Compiler cannot us in ensuring these have internationalisation strings, or that a particular string
 * is never used.
 * These unit tests cover those holes for the following entities:
 * <ul>
 *     <li>SoundSets</li>
 *     <li>Themes</li>
 *     <li>Melodies</li>
 * </ul>
 * </p>
 */
public class DynamicTranslationIdentifierTest {
    /**
     * Ensures that all available {@link com.nicobrailo.pianoli.sound.SampledSoundSet}s are translatable.
     *
     * <p>
     * It does so by checking if each "soundset_FOO" folder under <code>src/main/assets/</code> has
     * a matching entry in <code>src/main/res/values/strings.xml</code>.
     * </p>
     * <p>
     * We do <em>not</em> test if the translation exists in all languages; test-failures due to (lack of) translation
     * progress is a distraction, and not something the developer can control.
     * Instead, we ensure that
     * </p>
     *
     * @see com.nicobrailo.pianoli.sound.SampledSoundSet
     * @see SoundSet#PREFIX
     * @see SoundSet#getAvailableSoundsets(AssetManager)
     * @see SettingsFragment#loadSounds()
     */
    @ParameterizedTest
    @MethodSource("getSoundSets")
    public void testSoundsetsHaveTranslationEntities(Path soundsetAssetFolder) {
        List<String> translatables = getSoundSetTranslations();

        String folderName = soundsetAssetFolder.getFileName().toString();

        assertContains(translatables, folderName,
                "Asset folder '" + soundsetAssetFolder + "' has no translation string in app/src/main/res/values/strings.xml");
    }

    /**
     * Ensures our translations actually have a soundset backing them.
     *
     * <p>
     * Inverse of {@link #testSoundsetsHaveTranslationEntities(Path)}, useful if we rename or delete asset folders.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("getSoundSetTranslations")
    public void testNoLeftoverSoundSetTranslations(String translationIdentifier) throws IOException {
        List<String> soundSetAssets = getSoundSets().stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());

        assertContains(soundSetAssets, translationIdentifier,
                "Translation id '" + translationIdentifier + "' translates a soundset that doesn't exist in src/main/assets/");
    }


    /**
     * Ensures that all specific {@link Theme}s are translatable.
     *
     * @see Theme
     * @see Theme#PREFIX
     * @see Theme#fromPreferences(Context)
     * @see Preferences#selectedTheme(Context)
     * @see R.xml#root_preferences
     */
    @ParameterizedTest
    @MethodSource("getThemes")
    public void testThemesHaveTranslationEntities(String themeName) {
        List<String> translatables = getThemeTranslations();

        String matchingForm = Theme.PREFIX + themeName.toLowerCase(Locale.ROOT);
        assertContains(translatables, matchingForm,
                "Theme '" + themeName + "' has no translation string ('" + matchingForm + "') in app/src/main/res/values/strings.xml");
    }

    /**
     * Ensures our translations actually have a Theme backing them.
     *
     * <p>
     * Inverse of {@link #testThemesHaveTranslationEntities(String)}, useful if we rename or delete themes.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("getThemeTranslations")
    public void testNoLeftoverThemeTranslations(String translationIdentifier) {
        List<String> themes = getThemes();

        String matchingForm = translationIdentifier
                .replaceFirst("^" + Theme.PREFIX, "")
                .toUpperCase(Locale.ROOT);
        assertContains(themes, matchingForm,
                "Translation id '" + translationIdentifier + "' translates a theme that doesn't exist in Theme.java " +
                        "(" + themes + ")");
    }


    /**
     * Ensures that all specific {@link Melody Melodies} are translatable.
     *
     * @see Melody
     * @see Melody#PREFIX
     * @see Melody#all
     * @see SettingsFragment#loadMelodies()
     */
    @ParameterizedTest
    @MethodSource("getMelodies")
    public void testMelodiesHaveTranslationEntities(String melodyId) {
        List<String> translatables = getMelodyTranslations();

        String matchingForm = Melody.PREFIX + melodyId.toLowerCase(Locale.ROOT);
        assertContains(translatables, matchingForm,
                "Melody '" + melodyId + "' has no translation string ('" + matchingForm + "') in app/src/main/res/values/strings.xml");
    }


    /**
     * Ensures our translations actually have a melody backing them.
     *
     * <p>
     * Inverse of {@link #testMelodiesHaveTranslationEntities(String)}, useful if we rename or delete melodies.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("getMelodyTranslations")
    public void testNoLeftoverMelodyTranslations(String translationIdentifier) {
        List<String> melodies = getMelodies();

        String matchingForm = translationIdentifier
                .replaceFirst("^" + Melody.PREFIX, "");

        assertContains(melodies, matchingForm,
                "Translation id '" + translationIdentifier + "' translates a melody that doesn't exist in Melody.java " +
                        "(" + melodies + ")");
    }

    /**
     * Scans the primary translation string source for identifiers starting with <code>prefix</code>
     */
    private static List<String> getTranslationsByPrefix(String prefix) {
        // All String-resource identifiers
        Field[] allStrings = R.string.class.getFields();

        return Arrays.stream(allStrings)
                .map(Field::getName)
                .filter(name -> name.startsWith(prefix))
                .collect(Collectors.toList());
    }

    /**
     * {@link MethodSource} for all soundset translation identifiers
     */
    public static List<String> getSoundSetTranslations() {
        return getTranslationsByPrefix(SoundSet.PREFIX);
    }

    /**
     * {@link MethodSource} for all soundset asset folders
     */
    @NotNull
    public static List<Path> getSoundSets() throws IOException {
        Path soundAssets = Paths.get("src/main/assets/sounds"); // app-tests run in app-folder (at least on my IDE)

        try (Stream<Path> pathStream = Files.list(soundAssets)) {
            return pathStream
                    .filter(Files::isDirectory) // skip top-level files (specifically: "source" attribution file)
                    .filter(path -> path.getFileName().toString().startsWith(SoundSet.PREFIX))
                    .collect(Collectors.toList());
        }
    }

    /**
     * {@link MethodSource} for all theme translation identifiers
     */
    public static List<String> getThemeTranslations() {
        return getTranslationsByPrefix(Theme.PREFIX);
    }

    /**
     * {@link MethodSource} for all themes known to PianOli
     *
     * @see Theme
     */
    public static List<String> getThemes() {
        return Arrays.stream(Theme.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }


    /**
     * {@link MethodSource} for all melody translation identifiers
     */
    public static List<String> getMelodyTranslations() {
        return getTranslationsByPrefix(Melody.PREFIX);
    }

    public static List<String> getMelodies() {
        return Arrays.stream(Melody.all)
                .map(Melody::getId)
                .collect(Collectors.toList());
    }
}
