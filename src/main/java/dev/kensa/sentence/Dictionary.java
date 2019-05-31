package dev.kensa.sentence;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.Integer.compare;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public final class Dictionary {

    private static final Pattern NO_MATCH_PATTERN = Pattern.compile(".^");

    private final Set<Acronym> acronyms = new LinkedHashSet<>();
    private final Set<String> keywords = new LinkedHashSet<>(asList("given", "when", "then", "and", "with", "that"));

    public void putAcronyms(Acronym... acronyms) {
        Objects.requireNonNull(acronyms);

        this.acronyms.addAll(asList(acronyms));
    }

    public void putKeyword(String value) {
        if (value == null || value.length() < 2) {
            throw new IllegalArgumentException(String.format("Keywords must be at least 2 characters. [%s]", value));
        }
        keywords.add(value);
    }

    public void clearAcronyms() {
        acronyms.clear();
    }

    public void putKeywords(String... values) {
        asList(values).forEach(this::putKeyword);
    }

    public Set<Acronym> acronyms() {
        return acronyms;
    }

    public Set<String> keywords() {
        return keywords;
    }

    public Pattern acronymPattern() {
        Set<Acronym> acronyms = new LinkedHashSet<>(this.acronyms);
        return acronyms.isEmpty() ? NO_MATCH_PATTERN :
                Pattern.compile(
                        acronyms.stream()
                                .map(Acronym::acronym)
                                .sorted(longestFirst())
                                .collect(joining("|"))
                );
    }

    public Pattern keywordPattern() {
        Set<String> keywords = new LinkedHashSet<>(this.keywords);
        return Pattern.compile(
                keywords.stream()
                        .sorted(longestFirst())
                        .collect(joining("|", "^(", ")"))
        );
    }

    private Comparator<String> longestFirst() {
        return (a1, a2) -> compare(a2.length(), a1.length());
    }
}
