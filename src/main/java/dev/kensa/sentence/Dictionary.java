package dev.kensa.sentence;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

public final class Dictionary {

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

    public Set<String> acronymStrings() {
        return acronyms.stream().map(Acronym::acronym).collect(toSet());
    }
}
