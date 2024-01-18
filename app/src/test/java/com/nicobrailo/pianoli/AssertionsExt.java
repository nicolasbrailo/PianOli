package com.nicobrailo.pianoli;

import org.junit.jupiter.api.Assertions;

import java.util.Collection;

public class AssertionsExt {
    /**
     * Fails (with <code>message</code>) if collection <code>haystack</code> does not contain <code>needle</code>.
     */
    public static <T> void assertContains(Collection<T> haystack, T needle, String message) {
        if (!haystack.contains(needle)) {
            Assertions.fail(message);
        }
    }
}
