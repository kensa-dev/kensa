package dev.kensa.sentence;

import dev.kensa.sentence.scanner.Index;
import dev.kensa.sentence.scanner.TokenScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static dev.kensa.sentence.Token.Type.*;

public class SentenceBuilder {

    private final List<SentenceToken> tokens = new ArrayList<>();
    private final TokenScanner scanner;
    private final Set<String> highlightedValues;
    private final Set<String> acronyms;

    private int lastLineNumber;

    public SentenceBuilder(int startLine, Set<String> highlightedValues, Set<String> keywordPattern, Set<String> acronyms) {
        lastLineNumber = startLine;
        this.acronyms = acronyms;
        scanner = new TokenScanner(highlightedValues, keywordPattern, acronyms);
        this.highlightedValues = highlightedValues;
    }

    public SentenceBuilder appendIdentifier(String value) {
        if (highlightedValues.contains(value)) {
            if (isAcronym(value)) {
                append(value, HighlightedAcronym);
            } else {
                append(value, HighlightedIdentifier);
            }
        } else if (isAcronym(value)) {
            append(value, IdentifierAcronym);
        } else {
            append(value, Identifier);
        }

        return this;
    }

    // A String Literal - any arbitrary string literal found within the source code
    public SentenceBuilder appendStringLiteral(String value) {
        if (isAcronym(value)) {
            append(value, StringLiteralAcronym);
        } else {
            append(value, StringLiteral);
        }

        return this;
    }

    // A Literal - values such as true, false, 10, 5L, 0.5f or null found within the source code
    public SentenceBuilder appendLiteral(String value) {
        append(value, Literal);

        return this;
    }

    public SentenceBuilder append(String value) {
        scanner.scan(value).stream()
               .forEach(index -> {
                   String rawToken = value.substring(index.start(), index.end());
                   String tokenValue = tokenValueFor(index, rawToken);

                   append(tokenValue, index.type());
               });

        return this;
    }

    public SentenceBuilder markLineNumber(int thisLineNumber) {
        if (thisLineNumber > lastLineNumber) {
            lastLineNumber = thisLineNumber;
            appendNewLine();
        }

        return this;
    }

    public Sentence build() {
        return new Sentence(tokens);
    }

    private void append(String value, Token.Type literal) {
        tokens.add(new SentenceToken(literal, value));
    }

    private void appendNewLine() {
        append("", NewLine);
    }

    private String tokenValueFor(Index index, String rawToken) {
        String tokenValue = rawToken;
        if (index.type().isAcronym()) {
            tokenValue = tokenValue.toUpperCase();
        } else if (index.type() == Keyword) {
            if (tokens.size() == 0) {
                tokenValue = Character.toUpperCase(rawToken.charAt(0)) + rawToken.substring(1);
            }
        } else if (index.type() == Word) {
            tokenValue = Character.toLowerCase(rawToken.charAt(0)) + rawToken.substring(1);
        }
        return tokenValue;
    }

    private boolean isAcronym(String value) {
        return acronyms.stream()
                       .anyMatch(acronym -> acronym.equals(value));
    }
}
