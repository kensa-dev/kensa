package dev.kensa.sentence;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SentenceTest {

    @Test
    void squashesTokensContainingWordsOnly() {
        List<SentenceToken> expected = List.of(SentenceTokens.aWordOf("Word1 Word2 Word3 Word4"));
        Sentence sentence = new Sentence(List.of(SentenceTokens.aWordOf("Word1"), SentenceTokens.aWordOf("Word2"), SentenceTokens.aWordOf("Word3"), SentenceTokens.aWordOf("Word4")));

        List<SentenceToken> squashed = sentence.squashedTokens().collect(Collectors.toList());

        assertThat(squashed).containsExactlyElementsOf(expected);
    }

    @Test
    void squashesTokensContainingWordsAndOtherTypes() {
        List<SentenceToken> expected = List.of(
                SentenceTokens.anIdentifierOf("P1"), SentenceTokens.aWordOf("Word1 Word2"), SentenceTokens.aStringLiteralOf("L1"), SentenceTokens.aLiteralOf("null"), SentenceTokens.aKeywordOf("K1"), SentenceTokens
                        .aNewline(), SentenceTokens.anAcronymOf("FOO"), SentenceTokens.aKeywordOf("K2"), SentenceTokens.aWordOf("Word3 Word4"),
                SentenceTokens.anAcronymOf("BOO"), SentenceTokens.aLiteralOf("true")
        );
        Sentence sentence = new Sentence(
                List.of(SentenceTokens.anIdentifierOf("P1"), SentenceTokens.aWordOf("Word1"), SentenceTokens.aWordOf("Word2"), SentenceTokens.aStringLiteralOf("L1"), SentenceTokens.aLiteralOf("null"), SentenceTokens
                                .aKeywordOf("K1"), SentenceTokens.aNewline(), SentenceTokens.anAcronymOf("FOO"), SentenceTokens.aKeywordOf("K2"), SentenceTokens.aWordOf("Word3"),
                        SentenceTokens.aWordOf("Word4"), SentenceTokens.anAcronymOf("BOO"), SentenceTokens.aLiteralOf("true")
                ));

        List<SentenceToken> squashed = sentence.squashedTokens().collect(Collectors.toList());

        assertThat(squashed).containsExactlyElementsOf(expected);
    }

}