package dev.kensa.sentence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.kensa.sentence.SentenceTokens.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class SentenceTest {

    @Test
    void squashesTokensContainingWordsOnly() {
        List<SentenceToken> expected = List.of(aWordOf("Word1 Word2 Word3 Word4"));
        Sentence sentence = new Sentence(
                List.of(
                        aWordOf("Word1"),
                        aWordOf("Word2"),
                        aWordOf("Word3"),
                        aWordOf("Word4")
                )
        );

        List<SentenceToken> squashed = sentence.squashedTokens().collect(toList());

        assertThat(squashed).containsExactlyElementsOf(expected);
    }

    @Test
    void squashesTokensContainingWordsAndOtherTypes() {
        List<SentenceToken> expected = List.of(
                anIdentifierOf("P1"),
                aWordOf("Word1 Word2"),
                aStringLiteralOf("L1"),
                aLiteralOf("null"),
                aKeywordOf("K1"),
                aStringLiteralAcronymOf("LA1"),
                aNewline(),
                anAcronymOf("FOO"),
                aKeywordOf("K2"),
                aWordOf("Word3 Word4"),
                anAcronymOf("BOO"),
                aLiteralOf("true")
        );
        Sentence sentence = new Sentence(
                List.of(
                        anIdentifierOf("P1"),
                        aWordOf("Word1"),
                        aWordOf("Word2"),
                        aStringLiteralOf("L1"),
                        aLiteralOf("null"),
                        aKeywordOf("K1"),
                        aStringLiteralAcronymOf("LA1"),
                        aNewline(),
                        anAcronymOf("FOO"),
                        aKeywordOf("K2"),
                        aWordOf("Word3"),
                        aWordOf("Word4"),
                        anAcronymOf("BOO"),
                        aLiteralOf("true")
                ));

        List<SentenceToken> squashed = sentence.squashedTokens().collect(toList());

        assertThat(squashed).containsExactlyElementsOf(expected);
    }

}