package dev.kensa.sentence;

import java.util.EnumSet;
import java.util.Set;

public interface Token {
    Type type();

    String asString();

    enum Type {
        Acronym,
        HighlightedAcronym,
        HighlightedWord,
        HighlightedIdentifier,
        Identifier,
        IdentifierAcronym,
        Keyword,
        Literal,
        NewLine,
        StringLiteral,
        StringLiteralAcronym,
        Word;

        private static final Set<Type> ACRONYM_TYPES = EnumSet.of(Acronym, HighlightedAcronym, IdentifierAcronym, StringLiteralAcronym);

        boolean isAcronym() {
            return ACRONYM_TYPES.contains(this);
        }
    }
}
