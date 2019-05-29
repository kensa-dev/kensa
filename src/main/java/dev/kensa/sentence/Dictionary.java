package dev.kensa.sentence;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

    public Stream<String> keywords() {
        return keywords.stream();
    }

    public Pattern acronymPattern() {
        return acronyms.isEmpty() ? NO_MATCH_PATTERN :
                Pattern.compile(
                        acronyms
                                .stream()
                                .map(Acronym::acronym)
                                .sorted((a1, a2) -> compare(a2.length(), a1.length())) // ** Important: Longest first
                                .collect(joining("|"))
                );
    }

    public Pattern keywordPattern() {
        return Pattern.compile(
                keywords()
                        .sorted((a1, a2) -> compare(a2.length(), a1.length())) // ** Important: Longest first
                        .collect(joining("|", "^(", ")"))
        );
    }
}
