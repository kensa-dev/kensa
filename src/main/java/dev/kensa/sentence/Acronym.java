package dev.kensa.sentence;

import java.util.Objects;

public class Acronym {

    public static Acronym of(String acronym, String meaning) {
        if (acronym == null || acronym.length() < 2) {
            throw new IllegalArgumentException(String.format("Acronyms must be at least 2 characters. [%s]", acronym));
        }

        return new Acronym(acronym, meaning);
    }

    private final String acronym;
    private final String meaning;

    private Acronym(String acronym, String meaning) {
        this.acronym = acronym;
        this.meaning = meaning;
    }

    public String acronym() {
        return acronym;
    }

    public String meaning() {
        return meaning;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Acronym)) {
            return false;
        }
        Acronym acronym1 = (Acronym) o;
        return acronym.equals(acronym1.acronym);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acronym);
    }
}
