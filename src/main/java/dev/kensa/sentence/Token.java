package dev.kensa.sentence;

public interface Token {
    Type type();
    String asString();

    enum Type {
        Keyword,
        Acronym,
        Parameter,
        Word
    }
}
