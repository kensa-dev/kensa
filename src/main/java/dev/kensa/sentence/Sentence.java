package dev.kensa.sentence;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static dev.kensa.sentence.Token.Type.Word;

public class Sentence {

    private final List<SentenceToken> tokens;

    public Sentence(List<SentenceToken> tokens) {
        this.tokens = tokens;
    }

    public Stream<SentenceToken> stream() {
        return tokens.stream();
    }

    public Stream<SentenceToken> squashedTokens() {
        List<SentenceToken> squashed = new ArrayList<>();
        Token.Type currentTokenType = null;
        String currentValue = "";

        for (SentenceToken token : tokens) {
            switch (token.type()) {
                case Acronym:
                case Keyword:
                case Parameter:
                case Literal:
                    if (currentTokenType == Word) {
                        squashed.add(new SentenceToken(currentTokenType, currentValue));
                        currentValue = "";
                    }
                    squashed.add(token);
                    break;
                case Word:
                    if (currentTokenType == Word) {
                        currentValue += " " + token.asString();
                    } else {
                        currentValue += token.asString();
                    }
                    break;
            }

            currentTokenType = token.type();
        }

        if (currentTokenType == Word) {
            squashed.add(new SentenceToken(currentTokenType, currentValue));
        }

        return squashed.stream();
    }
}
