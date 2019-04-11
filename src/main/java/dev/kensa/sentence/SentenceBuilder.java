package dev.kensa.sentence;

import dev.kensa.sentence.scanner.Index;
import dev.kensa.sentence.scanner.TokenScanner;

import java.util.ArrayList;
import java.util.List;

public class SentenceBuilder {

    private final List<SentenceToken> tokens = new ArrayList<>();
    private final TokenScanner scanner;

    public SentenceBuilder() {scanner = new TokenScanner();}

    public SentenceBuilder appendParameter(String value) {
        tokens.add(new SentenceToken(Token.Type.Parameter, value));

        return this;
    }

    public SentenceBuilder appendLiteral(String value) {
        tokens.add(new SentenceToken(Token.Type.Literal, value));

        return this;
    }

    public SentenceBuilder append(String value) {
        scanner.scan(value).stream()
               .forEach(index -> {
                   String rawToken = value.substring(index.start(), index.end());
                   String tokenValue = tokenValueFor(index, rawToken);

                   tokens.add(new SentenceToken(index.type(), tokenValue));
               });

        return this;
    }

    private String tokenValueFor(Index index, String rawToken) {
        String tokenValue = rawToken;
        if (index.type() == Token.Type.Word) {
            if(tokens.size() == 0) {
                 tokenValue = Character.toUpperCase(rawToken.charAt(0)) + rawToken.substring(1);
            } else {
                 tokenValue = Character.toLowerCase(rawToken.charAt(0)) + rawToken.substring(1);
            }
        }
        return tokenValue;
    }

    public Sentence build() {
        return new Sentence(tokens);
    }
}
