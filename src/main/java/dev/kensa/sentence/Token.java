package dev.kensa.sentence;

public interface Token {
    Type type();
    String asString();

    enum Type {
        Acronym,
        HighlightedWord,
        HighlightedIdentifier,
        Identifier,
        Keyword,
        Literal,
        NewLine,
        StringLiteral,
        Word
    }
}
