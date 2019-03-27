package dev.kensa.sentence;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static dev.kensa.sentence.Token.Type.*;
import static org.assertj.core.api.Assertions.assertThat;

class SentenceTest {

    @Test
    void squashesTokensContainingWordsOnly() {
        List<SentenceToken> expected = List.of(wordOf("Word1 Word2 Word3 Word4"));
        Sentence sentence = new Sentence(List.of(wordOf("Word1"), wordOf("Word2"), wordOf("Word3"), wordOf("Word4")));

        List<SentenceToken> squashed = sentence.squashedTokens().collect(Collectors.toList());

        assertThat(squashed).containsExactlyElementsOf(expected);
    }

    @Test
    void squashesTokensContainingWordsAndOtherTypes() {
        List<SentenceToken> expected = List.of(parameterOf("P1"), wordOf("Word1 Word2"), keywordOf("K1"), acronymOf("FOO"), keywordOf("K2"), wordOf("Word3 Word4"), acronymOf("BOO"));
        Sentence sentence = new Sentence(List.of(parameterOf("P1"), wordOf("Word1"), wordOf("Word2"), keywordOf("K1"), acronymOf("FOO"), keywordOf("K2"), wordOf("Word3"), wordOf("Word4"), acronymOf("BOO")));

        List<SentenceToken> squashed = sentence.squashedTokens().collect(Collectors.toList());

        assertThat(squashed).containsExactlyElementsOf(expected);
    }

    private SentenceToken wordOf(String word) {
        return tokenOf(Word, word);
    }

    private SentenceToken acronymOf(String acronym) {
        return tokenOf(Acronym, acronym);
    }

    private SentenceToken keywordOf(String acronym) {
        return tokenOf(Keyword, acronym);
    }

    private SentenceToken parameterOf(String acronym) {
        return tokenOf(Parameter, acronym);
    }

    private SentenceToken tokenOf(Token.Type type, String value) {
        return new SentenceToken(type, value);
    }
}