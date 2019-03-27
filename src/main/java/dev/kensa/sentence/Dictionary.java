package dev.kensa.sentence;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public final class Dictionary {

    private static final List<String> ONE_LETTER_WORDS = List.of("A", "a", "I");
    private static final List<String> ACRONYMS = new ArrayList<>();
    private static final List<String> KEYWORDS = new ArrayList<>(List.of("given", "when", "then", "and"));

    public static void putAcronym(String value) {
        if(value.length() < 2) {
            throw new IllegalArgumentException(String.format("Acronyms must be at least 2 characters. [%s]", value));
        }
        ACRONYMS.add(value);
    }

    public static void putKeyword(String value) {
        if(value.length() < 2) {
            throw new IllegalArgumentException(String.format("Keywords must be at least 2 characters. [%s]", value));
        }
        KEYWORDS.add(value);
    }

    public static void clearAcronyms() {
        ACRONYMS.clear();
    }

    public static void putAcronyms(String... values) {
        asList(values).forEach(Dictionary::putAcronym);
    }

    public static void putKeywords(String... values) {
        asList(values).forEach(Dictionary::putKeyword);
    }

    public static boolean isAcronym(String value) {
        return ACRONYMS.contains(value);
    }

    public static boolean isKeyword(String value) {
        return KEYWORDS.contains(value);
    }

    public static Stream<String> acronyms() {
        return ACRONYMS.stream();
    }

    public static Stream<String> keywords() {
        return KEYWORDS.stream();
    }
}
