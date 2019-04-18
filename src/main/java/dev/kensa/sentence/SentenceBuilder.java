package dev.kensa.sentence;

import dev.kensa.sentence.scanner.Index;
import dev.kensa.sentence.scanner.TokenScanner;

import java.util.ArrayList;
import java.util.List;

import static dev.kensa.sentence.Token.Type.*;

public class SentenceBuilder {

    private final List<SentenceToken> tokens = new ArrayList<>();
    private final TokenScanner scanner;

    public SentenceBuilder() {scanner = new TokenScanner();}

    public SentenceBuilder appendIdentifier(String value) {
        append(value, Identifier);

        return this;
    }

    // A String Literal - any arbitrary string literal found within the source code
    public SentenceBuilder appendStringLiteral(String value) {
        append(value, StringLiteral);

        return this;
    }

    // A Literal - values such as true, false, 10, 5L, 0.5f or null found within the source code
    public SentenceBuilder appendLiteral(String value) {
        append(value, Literal);

        return this;
    }

    public SentenceBuilder appendNewLine() {
        append("", NewLine);

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

    public Sentence build() {
        return new Sentence(tokens);
    }

    private void append(String value, Token.Type literal) {
        tokens.add(new SentenceToken(literal, value));
    }

    private String tokenValueFor(Index index, String rawToken) {
        String tokenValue = rawToken;
        if (index.type() == Keyword) {
            if (tokens.size() == 0) {
                tokenValue = Character.toUpperCase(rawToken.charAt(0)) + rawToken.substring(1);
            }
        } else {
            tokenValue = Character.toLowerCase(rawToken.charAt(0)) + rawToken.substring(1);
        }
        return tokenValue;
    }
}
