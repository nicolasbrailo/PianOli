package com.nicobrailo.pianoli;

import android.content.res.AssetManager;
import com.nicobrailo.pianoli.sound.SoundSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Consistency checks for dynamically-accessed entities: do they have translation-strings?
 *
 * <p>There are a few entities in PianOli that are derived dynamically at runtime.
 * The IDE/Compiler cannot us in ensuring these have internationalisation strings, or that a particular string
 * is never used.
 * These unit tests cover those holes for the following entities:
 * <ul>
 *     <li>SoundSets</li>
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
     * Fails (with <code>message</code>) if collection <code>haystack</code> does not contain <code>needle</code>.
     */
    public <T> void assertContains(Collection<T> haystack, T needle, String message) {
        if (!haystack.contains(needle)) {
            Assertions.fail(message);
        }
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
}
