package com.nicobrailo.pianoli.sound;

import com.nicobrailo.pianoli.sound.SoundSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoundSetPrefixTest {
    @Test
    public void addPrefix() {
        assertEquals("soundset_foo", SoundSet.addPrefix("foo"));
    }

    @Test
    public void addPrefixDoesntDouble() {
        assertEquals("soundset_foo", SoundSet.addPrefix("soundset_foo"));
    }

    @Test
    public void stripPrefix() {
        assertEquals("foo", SoundSet.stripPrefix("soundset_foo"));
    }

    @Test
    public void stripPrefixDoesntDouble() {
        assertEquals("foo", SoundSet.stripPrefix("foo"));
    }
}
