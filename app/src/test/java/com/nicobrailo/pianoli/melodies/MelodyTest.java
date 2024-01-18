package com.nicobrailo.pianoli.melodies;

import com.nicobrailo.pianoli.AssertionsExt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MelodyTest {

    /** JUnit5 arguments-adapter for {@link Melody} content */
    private static Stream<Arguments> enumerateAllMelodies() {
        return Arrays.stream(Melody.all)
                .map(m -> Arguments.of(m.getId(), m.getNotes()));
    }

    @ParameterizedTest(name = "[{index}] {0}") // name: avoid default note-array in testname, only songId
    @MethodSource("enumerateAllMelodies")
    public void allSongsParsable(String songId, int[] notes) {
            int noteIndex = 0;
            for (int note: notes) {
                assertNotEquals(NoteMapper.NO_NOTE, note,
                        String.format("Can't parse song %s, note at position %d not recognised",
                                songId, noteIndex));
                noteIndex++;
        }
    }

    @Test
    void garbageParsesAsNoNote() {
        Melody garbage = Melody.fromString("garbage", "foo bar baz xyzzy plugh quux");
        assertArrayEquals(new int[] {5,5,5,5,5,5}, garbage.getNotes());
    }

    @Test
    void getId() {
        String expected = "test-id";
        Melody withId = new Melody(expected, new int[0]);
        assertEquals(expected, withId.getId());
    }


    /**
     * Very poor substitute for classpath scanning: just iterate java-files in the right source folder
     *
     * <p>
     * Works because:<ol>
     * </ol>
     *      <li>only one folder to scan</li>
     *      <li>one song per java file convention</li>
     *      <li>filename to melody-id mapping convention</li>
     * </p>
     */
    public static List<Path> enumerateAllSongFiles() throws IOException {
        Path songSourceFolder = Paths.get("src", "main", "java", "com/nicobrailo/pianoli", "song");

        try (Stream<Path> songStream = Files.list(songSourceFolder)) {
            return songStream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
    }


    /**
     * Checks if all programmed melodies are also registered in the runtime-available list: {@link Melody#all}.
     *
     * <p>
     * This way, we can't forget the required manual step to make a song actually available at runtime.
     * </p>
     * <p>
     * We want to avoid classpath scanning for melodies, since that´s a very big dependency for such a tiny use-case.
     * Furthermore, the simple folder-scanning we do here is next to impossible to do at runtime, since the Android API
     * doesn't really give us raw file access.<br>
     * Thus, ensure our available-at-runtime-list is in sync with the filesystem during tests, when we have
     * 'normal', android-free java, and full filesystem access.
     * </p>
     *
     * @see Melody#all
     * @see #enumerateAllSongFiles()
     */
    @ParameterizedTest
    @MethodSource("enumerateAllSongFiles")
    void testAllSongsAreRegistered(Path songJavaPath) {
        List<String> registeredMelodiesMatchingFOrm = Arrays.stream(Melody.all)
                .map(Melody::getId)
                .map(id -> id.replaceAll("_", ""))
                .collect(Collectors.toList());

        String fileMatchingForm = songJavaPath
                .getFileName()
                .toString()
                .replaceFirst("\\.java$", "")
                .toLowerCase(Locale.ROOT);

        AssertionsExt.assertContains(registeredMelodiesMatchingFOrm, fileMatchingForm,
                "Song file " + songJavaPath + " is not registered in hardcoded Melody.all " +
                        "(" + Arrays.toString(Melody.all) + ")");
    }
}
