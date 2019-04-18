package dev.kensa.sentence;

import static dev.kensa.sentence.Token.Type.*;

public final class SentenceTokens {

    public static SentenceToken aWordOf(String word) {
        return aTokenOf(Word, word);
    }

    static SentenceToken aLiteralOf(String word) {
        return aTokenOf(Literal, word);
    }

    public static SentenceToken aNewline() {
        return aTokenOf(NewLine, "");
    }

    public static SentenceToken aStringLiteralOf(String word) {
        return aTokenOf(StringLiteral, word);
    }

    static SentenceToken anAcronymOf(String acronym) {
        return aTokenOf(Acronym, acronym);
    }

    public static SentenceToken aKeywordOf(String acronym) {
        return aTokenOf(Keyword, acronym);
    }

    public static SentenceToken aParameterOf(String acronym) {
        return aTokenOf(Identifier, acronym);
    }

    private static SentenceToken aTokenOf(Token.Type type, String value) {
        return new SentenceToken(type, value);
    }

    private SentenceTokens() {
    }
}
