package dev.kensa.sentence;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Integer.compare;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public final class Dictionary {

    private static final Pattern NO_MATCH_PATTERN = Pattern.compile(".^");
    private static final List<String> ONE_LETTER_WORDS = asList("A", "a", "I");
    private static final Set<String> ACRONYMS = new LinkedHashSet<>();
    private static final Set<String> KEYWORDS = new LinkedHashSet<>(asList("given", "when", "then", "and", "with", "that"));

    public static void putAcronym(String value) {
        if (value == null || value.length() < 2) {
            throw new IllegalArgumentException(String.format("Acronyms must be at least 2 characters. [%s]", value));
        }
        ACRONYMS.add(value);
    }

    public static void putKeyword(String value) {
        if (value == null || value.length() < 2) {
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

    public static Pattern acronymPattern() {
        return ACRONYMS.isEmpty() ? NO_MATCH_PATTERN :
                Pattern.compile(acronyms().sorted((a1, a2) -> compare(a2.length(), a1.length())) // ** Important: Longest first
                                          .collect(joining("|")));
    }

    public static Pattern keywordPattern() {
        return Pattern.compile(keywords().sorted((a1, a2) -> compare(a2.length(), a1.length())) // ** Important: Longest first
                                         .collect(joining("|", "^(", ")")));
    }
}
